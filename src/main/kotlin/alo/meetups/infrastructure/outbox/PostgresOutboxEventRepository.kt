package alo.meetups.infrastructure.outbox

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID
import javax.transaction.Status.*

open class PostgresOutboxEventRepository(
    private val jdbi: Jdbi,
    private val clock: Clock = Clock.systemUTC(),
    private val generateId: () -> UUID = { UUID.randomUUID() },
) : TransactionalOutbox {

    override fun storeForReliablePublishing(event: OutboxEvent) {
        jdbi.open().use { handle ->
            handle.ensureActiveTransaction()
            handle.execute(
                "INSERT INTO outbox (id, aggregate_id, event_payload, stream, created) VALUES (?,?,?,?,?)",
                generateId(),
                event.aggregateId,
                event.payload,
                event.stream,
                LocalDateTime.now(clock)
            )
        }
    }

    override fun findReadyForPublishing(batchSize: Int): List<OutboxEvent> {
        return jdbi.open().use {
            it.ensureActiveTransaction()
            it.createQuery(
                """
            DELETE FROM outbox
            WHERE aggregate_id IN ( SELECT aggregate_id FROM outbox ORDER BY created ASC LIMIT $batchSize FOR UPDATE ) 
            RETURNING *
            """
            ).map { rs, _ ->
                OutboxEvent(
                    aggregateId = UUID.fromString(rs.getString("aggregate_id")),
                    payload = rs.getBytes("event_payload"),
                    stream = rs.getString("stream")
                )
            }.list()
        }
    }

    private fun Handle.ensureActiveTransaction() {
        if (!jdbi.transactionHandler.isInTransaction(this)) throw MandatoryActiveTransactionException
    }
}
