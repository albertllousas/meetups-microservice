package alo.meetups.infrastructure.config

import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.Transactional
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.infrastructure.UseCaseDecorator
import alo.meetups.infrastructure.adapters.output.db.JTATransactional
import alo.meetups.infrastructure.adapters.output.db.PostgresGroupRepository
import alo.meetups.infrastructure.adapters.output.db.PostgresMeetupRepository
import alo.meetups.infrastructure.adapters.output.http.UsersHttpClient
import alo.meetups.infrastructure.adapters.output.pubsub.HandleLogging
import alo.meetups.infrastructure.adapters.output.pubsub.HandleMetrics
import alo.meetups.infrastructure.adapters.output.pubsub.HandleOutbox
import alo.meetups.infrastructure.adapters.output.pubsub.PublishEventInMemory
import alo.meetups.infrastructure.outbox.TransactionalOutbox
import io.agroal.api.AgroalDataSource
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jdbi.v3.core.Jdbi
import java.time.Clock
import java.util.Properties
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject

@ApplicationScoped
class InfrastructureConfig() {

    @Inject
    lateinit var jdbi: Jdbi

    @Inject
    lateinit var meterRegistry: MeterRegistry

    @Inject
    lateinit var dataSource: AgroalDataSource

    @Inject
    lateinit var handleOutbox: HandleOutbox

    @Produces
    @ApplicationScoped
    fun kafkaProducer(
        @ConfigProperty(name = "kafka.bootstrap.servers") bootstrapServers: String
    ): KafkaProducer<String, ByteArray> = KafkaProducer<String, ByteArray>(
        Properties().apply {
            this["key.serializer"] = StringSerializer::class.java.name
            this["value.serializer"] = ByteArraySerializer::class.java.name
            this["bootstrap.servers"] = bootstrapServers
        }
    )

    @Produces
    @ApplicationScoped
    fun jdbi(): Jdbi = Jdbi.create(dataSource)

    @Produces
    @ApplicationScoped
    fun useCaseDecorator(): UseCaseDecorator = UseCaseDecorator()

    @Produces
    @ApplicationScoped
    fun clock(): Clock = Clock.systemUTC()

    @Produces
    @ApplicationScoped
    fun findUser(@ConfigProperty(name = "clients.users-service.url") baseUrl: String): FindUser =
        UsersHttpClient(baseUrl)

    @Produces
    @ApplicationScoped
    fun meetupRepository(): MeetupRepository = PostgresMeetupRepository(jdbi)

    @Produces
    @ApplicationScoped
    fun groupRepository(): GroupRepository = PostgresGroupRepository(jdbi)

    @Produces
    @ApplicationScoped
    fun publishEvent(): PublishEvent = PublishEventInMemory(
        handlers = listOf(HandleLogging(), HandleMetrics(meterRegistry), handleOutbox)
    )
}