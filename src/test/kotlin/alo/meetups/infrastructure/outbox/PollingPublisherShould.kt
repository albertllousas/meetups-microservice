package alo.meetups.infrastructure.outbox

import alo.meetups.fixtures.OutboxEventBuilder
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.helpers.NOPLogger
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit
import javax.transaction.UserTransaction

class PollingPublisherShould {

    private val logger = spyk(NOPLogger.NOP_LOGGER)

    private val meterRegistry = SimpleMeterRegistry()

    private val outboxEventRepository = mockk<PostgresOutboxEventRepository>(relaxed = true)

    private val messageRelay = mockk<MessageRelay>(relaxed = true)


    private val userTransaction = spyk(object : UserTransaction {
        override fun begin() {}
        override fun commit() {}
        override fun rollback() {}
        override fun setRollbackOnly() {}
        override fun getStatus(): Int = 0
        override fun setTransactionTimeout(seconds: Int) {}
    })

    init {
        PollingPublisher(
            transactionalOutbox = outboxEventRepository,
            messageRelay = messageRelay,
            pollingIntervalMs = 50,
            userTransaction = userTransaction,
            batchSize = 5,
            meterRegistry = meterRegistry,
            logger = logger
        )
    }

    @Test
    fun `send eventually events to kafka when outbox have some of them ready to be sent`() {
        val outboxEvent = OutboxEventBuilder.build()
        every { outboxEventRepository.findReadyForPublishing(5) } returns listOf(outboxEvent)

        verify(timeout = 2000) {
            messageRelay.send(listOf(outboxEvent))
            userTransaction.commit()
        }
    }

    @Test
    fun `not delete from the outbox the message to be sent when there is a problem sending it`() {
        val outboxEvent = OutboxEventBuilder.build()
        val crash = RuntimeException("Boom!")
        every { outboxEventRepository.findReadyForPublishing(5) } returns listOf(outboxEvent)
        every { messageRelay.send(listOf(outboxEvent)) } throws crash

        verify(timeout = 2000) {
            logger.error("Message batch publishing failed, will be retried", crash)
            userTransaction.rollback()
        }
        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            assertThat(meterRegistry.counter("outbox.publishing.fail").count()).isGreaterThan(0.0)
        }
    }
}
