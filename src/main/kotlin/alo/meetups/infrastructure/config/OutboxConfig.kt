package alo.meetups.infrastructure.config

import alo.meetups.infrastructure.adapters.output.pubsub.HandleOutbox
import alo.meetups.infrastructure.outbox.MessageRelay
import alo.meetups.infrastructure.outbox.PollingPublisher
import alo.meetups.infrastructure.outbox.PostgresOutboxEventRepository
import alo.meetups.infrastructure.outbox.TransactionalOutbox
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.producer.KafkaProducer
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jdbi.v3.core.Jdbi
import java.time.Clock
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.transaction.UserTransaction


@ApplicationScoped
class HandleOutboxConfig {

    @Inject
    lateinit var transactionalOutbox: TransactionalOutbox

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var clock: Clock

    @Produces
    @ApplicationScoped
    fun handleOutbox(
        @ConfigProperty(name = "mp.messaging.outgoing.groups-out.topic") groupStream: String,
        @ConfigProperty(name = "mp.messaging.outgoing.meetups-out.topic") meetupStream: String,
    ): HandleOutbox = HandleOutbox(
        transactionalOutbox, meetupStream, groupStream, objectMapper, clock
    )
}

@ApplicationScoped
class TransactionalOutboxConfig {

    @Inject
    lateinit var jdbi: Jdbi

    @Inject
    lateinit var clock: Clock

    @Produces
    @ApplicationScoped
    fun transactionalOutbox(): TransactionalOutbox = PostgresOutboxEventRepository(jdbi, clock)
}

@ApplicationScoped
class PollingPublisherConfig {

    @Inject
    lateinit var kafkaProducer: KafkaProducer<String, ByteArray>

    @Inject
    lateinit var transactionalOutbox: TransactionalOutbox

    @Inject
    lateinit var userTransaction: UserTransaction

    @Inject
    lateinit var meterRegistry: MeterRegistry

    @Produces
    @ApplicationScoped
    fun pollingPublisher() = PollingPublisher(
        transactionalOutbox = transactionalOutbox,
        messageRelay = MessageRelay(kafkaProducer),
        batchSize = 10,
        userTransaction = userTransaction,
        pollingIntervalMs = 1000,
        meterRegistry = meterRegistry
    )
}