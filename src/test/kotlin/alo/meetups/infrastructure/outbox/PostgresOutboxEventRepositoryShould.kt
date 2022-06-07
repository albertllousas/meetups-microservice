package alo.meetups.infrastructure.outbox

import alo.meetups.Postgres
import alo.meetups.fixtures.OutboxEventBuilder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.transaction.TransactionHandler
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class PostgresOutboxEventRepositoryShould {

    private val db = Postgres()

    private val stubbedTransactionHandler = mockk<TransactionHandler>().also {
        every { it.specialize(any()) } returns it
    }

    private val jdbi = Jdbi
        .create(db.container.jdbcUrl, db.container.username, db.container.password)
        .apply { transactionHandler = stubbedTransactionHandler }

    private val repository = PostgresOutboxEventRepository(jdbi)

    @Test
    fun `store an outbox event for publishing`() {
        val outboxEvent = OutboxEventBuilder.build()
        every { stubbedTransactionHandler.isInTransaction(any()) } returns true

        repository.storeForReliablePublishing(outboxEvent)

        val retrievedOutbox = jdbi.open()
            .createQuery("SELECT aggregate_id, event_payload, stream FROM outbox LIMIT 1")
            .map { rs, _ ->
                mapOf(
                    "aggregate_id" to rs.getObject("aggregate_id"),
                    "stream" to rs.getString("stream"),
                    "event_payload" to rs.getBytes("event_payload")
                )
            }
            .one()
        assertThat(retrievedOutbox).containsAllEntriesOf(
            mapOf(
                "aggregate_id" to outboxEvent.aggregateId,
                "stream" to outboxEvent.stream,
                "event_payload" to outboxEvent.payload
            )
        )
    }

    @Test
    fun `fail storing an outbox event when there is no alive transaction`() {
        val outboxEvent = OutboxEventBuilder.build()
        every { stubbedTransactionHandler.isInTransaction(any()) } returns false

        assertThatThrownBy { repository.storeForReliablePublishing(outboxEvent) }
            .isExactlyInstanceOf(MandatoryActiveTransactionException::class.java)
    }

    @Test
    fun `find last events ready for publishing and delete them`() {
        every { stubbedTransactionHandler.isInTransaction(any()) } returns true
        val firstEvent = OutboxEventBuilder.build().also(repository::storeForReliablePublishing)
        val secondEvent = OutboxEventBuilder.build().also(repository::storeForReliablePublishing)
        val thirdEvent = OutboxEventBuilder.build().also(repository::storeForReliablePublishing)

        val outboxMessage = repository.findReadyForPublishing(batchSize = 2)

        assertThat(outboxMessage).isEqualTo(listOf(firstEvent, secondEvent))
        val allIds = jdbi.open()
            .createQuery("SELECT aggregate_id FROM outbox")
            .map { rs, _ -> rs.getObject("aggregate_id") }
            .list()
        assertThat(allIds).containsOnly(thirdEvent.aggregateId)
    }

    @Test
    fun `ensure events found for publishing are blocked till they are processed`() {
        every { stubbedTransactionHandler.isInTransaction(any()) } returns true
        val oldestEvent = OutboxEventBuilder.build().also(repository::storeForReliablePublishing)
        val newestEvent = OutboxEventBuilder.build().also(repository::storeForReliablePublishing)

        val findReadyForPublishing = { repository.findReadyForPublishing(1) }

        val result = runBlocking(Dispatchers.IO) {
            val reallyLongExecution = async { findReadyForPublishing() }
            delay(50)
            val execution = async { findReadyForPublishing() }
            delay(50)
            val immediateExecution = async { findReadyForPublishing() }
            Triple(reallyLongExecution.await(), execution.await(), immediateExecution.await())
        }

        assertThat(result.first).isEqualTo(listOf(oldestEvent))
        assertThat(result.second).isEqualTo(listOf(newestEvent))
        assertThat(result.third).isEqualTo(emptyList<OutboxEvent>())
    }
}
