package alo.meetups.component.meetup

import alo.meetups.component.BaseComponentTest
import alo.meetups.fixtures.consumeAndAssert
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.ZonedDateTime
import java.util.UUID

@Tag("Component")
@QuarkusTest
class CreateMeetupShould : BaseComponentTest() {

    @Test
    fun `create an online meetup`() {
        val meetupId = UUID.randomUUID()
        val on = ZonedDateTime.now().plusDays(1)
        given()
            .contentType(JSON)
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

        kafkaConsumer.consumeAndAssert(stream = "meetups") { record ->
            assertThat(record.key()).isEqualTo(meetupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.MeetupCreatedEvent>(record.value()) }
        }
    }

    @Test
    fun `create an in person meetup`() {
        val meetupId = UUID.randomUUID()
        val on = ZonedDateTime.now().plusDays(1)
        given()
            .contentType(JSON)
            .body("""
               {
                    "id": "$meetupId",
                    "type": "in_person",
                    "host_id": "${UUID.randomUUID()}",
                    "topic": "Are introverts too quiet or extroverts too loud?",
                    "details": "Introverts vs extroverts",
                    "on": "$on",
                    "address": "34 Rd 45, Hawk Springs,wy, 82213  United States"
               }
            """)
            .`when`()
            .post("/meetups")
            .then()
            .statusCode(201)

        kafkaConsumer.consumeAndAssert(stream = "meetups") { record ->
            assertThat(record.key()).isEqualTo(meetupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.MeetupCreatedEvent>(record.value()) }
        }
    }
}