package alo.meetups.infrastructure.outbox

import kotlin.jvm.Throws

interface TransactionalOutbox {

    @Throws(MandatoryActiveTransactionException::class)
    fun storeForReliablePublishing(event: OutboxEvent)

    @Throws(MandatoryActiveTransactionException::class)
    fun findReadyForPublishing(batchSize: Int): List<OutboxEvent>
}

object MandatoryActiveTransactionException : RuntimeException(
    """Transactional outbox pattern needs an alive transaction to store in the event to send in the same local
        | transaction as the aggregate is saved, please, open a transaction to deal properly with the dual write.
    """
)

