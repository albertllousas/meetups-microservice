package alo.meetups.infrastructure.outbox

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.util.Timer
import javax.transaction.UserTransaction
import kotlin.concurrent.timerTask

private const val FAIL_COUNTER = "outbox.publishing.fail"

class PollingPublisher(
    private val transactionalOutbox: TransactionalOutbox,
    private val messageRelay: MessageRelay,
    private val batchSize: Int = 50,
    pollingIntervalMs: Long = 50L,
    private val userTransaction: UserTransaction,
    private val meterRegistry: MeterRegistry,
    private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()),
) {

    init {
        Timer().schedule(timerTask { this@PollingPublisher.publish() }, 100, pollingIntervalMs)
    }

    private fun publish() =
        try {
            userTransaction.begin()
            transactionalOutbox.findReadyForPublishing(batchSize)
                .also(messageRelay::send)
                .onEach { event -> logger.info("Aggregate-id: '${event.aggregateId}' event published to the stream '${event.stream}'") }
            userTransaction.commit()
        } catch (exception: Exception) {
            userTransaction.rollback()
            meterRegistry.counter(FAIL_COUNTER).increment()
            logger.error("Message batch publishing failed, will be retried", exception)
            throw exception
        }
}
