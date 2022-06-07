package alo.meetups.component

import alo.meetups.Kafka
import alo.meetups.Postgres
import alo.meetups.fixtures.buildKafkaConsumer
import alo.meetups.fixtures.buildKafkaProducer
import alo.meetups.fixtures.stubHttpEndpointForFindUserSucceeded
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector.MatchesType
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.BeforeEach
import java.time.ZonedDateTime
import java.util.UUID

class Resources : QuarkusTestResourceLifecycleManager {

    private lateinit var db: Postgres

    private lateinit var kafka: Kafka

    private lateinit var server: WireMockServer

    private lateinit var kafkaConsumer: KafkaConsumer<String, ByteArray>

    private lateinit var kafkaProducer: KafkaProducer<String, ByteArray>

    override fun start(): MutableMap<String, String> {
        db = Postgres()
        server = WireMockServer().also { it.start() }
        kafka = Kafka()
        kafkaConsumer = buildKafkaConsumer(kafka.container.bootstrapServers)
        kafkaProducer = buildKafkaProducer(kafka.container.bootstrapServers)
        return mutableMapOf(
            "quarkus.datasource.jdbc.url" to db.container.jdbcUrl,
            "clients.users-service.url" to server.baseUrl(),
            "kafka.bootstrap.servers" to kafka.container.bootstrapServers
        )
    }

    @Synchronized
    override fun stop() {
        db.container.stop()
        server.stop()
        kafka.container.stop()
    }

    override fun inject(testInjector: TestInjector) {
        testInjector.injectIntoFields(db, MatchesType(Postgres::class.java))
        testInjector.injectIntoFields(kafka, MatchesType(Kafka::class.java))
        testInjector.injectIntoFields(server, MatchesType(WireMockServer::class.java))
        testInjector.injectIntoFields(kafkaConsumer, MatchesType(KafkaConsumer::class.java))
        testInjector.injectIntoFields(kafkaProducer, MatchesType(KafkaProducer::class.java))
    }
}

@QuarkusTestResource(Resources::class)
abstract class BaseComponentTest {

    protected lateinit var db: Postgres

    protected lateinit var kafka: Kafka

    protected lateinit var wireMock: WireMockServer

    protected lateinit var kafkaConsumer: KafkaConsumer<String, ByteArray>

    protected lateinit var kafkaProducer: KafkaProducer<String, ByteArray>

    protected val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun `set up`() {
        wireMock.stubHttpEndpointForFindUserSucceeded()
    }

    protected fun givenAMeetupExists(): UUID {
        val meetupId = UUID.randomUUID()
        val on = ZonedDateTime.now().plusDays(1)
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""
               {
                    "id": "$meetupId",
                    "type": "online",
                    "host_id": "${UUID.randomUUID()}",
                    "topic": "Should toilet paper hang over or under the roll?",
                    "details": "Let's have a hot discussion about this endless topic",
                    "on": "$on",
                    "link_name": "Meeting: Toilet paper discussion",
                    "url": "https://some-meeting-platform/meetings/z4Xdv"
               }
            """)
            .`when`()
            .post("/meetups")
            .then()
            .statusCode(201)
        return meetupId
    }

    protected fun givenAMeetupIsFinished(meetupId: UUID) =
        RestAssured.given()
            .`when`()
            .patch("/meetups/$meetupId/finish")
            .then()
            .statusCode(204)

    protected fun givenAnAttendantToAMeetup(meetupId: UUID): UUID {
        val attendantId = UUID.randomUUID()
        RestAssured.given()
            .`when`()
            .contentType(ContentType.JSON)
            .body(""" { "attendant_id": "$attendantId"} """)
            .post("/meetups/$meetupId/attendants")
            .then()
            .statusCode(201)
        return attendantId
    }
}