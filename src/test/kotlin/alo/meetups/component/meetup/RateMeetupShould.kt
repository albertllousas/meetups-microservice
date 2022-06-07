package alo.meetups.component.meetup

import alo.meetups.component.BaseComponentTest
import alo.meetups.fixtures.consumeAndAssertMultiple
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@Tag("Component")
@QuarkusTest
class RateMeetupShould : BaseComponentTest() {

    @Test
    fun `rate a meetup`() {
        val meetupId = givenAMeetupExists()
        val attendantId = givenAnAttendantToAMeetup(meetupId)
        givenAMeetupIsFinished(meetupId)

        given()
            .`when`()
            .contentType(JSON)
            .body(""" { "attendant_id": "$attendantId", "score": 3 } """)
            .patch("/meetups/$meetupId/rate")
            .then()
            .statusCode(204)

        kafkaConsumer.consumeAndAssertMultiple(numberOfMessages = 4, stream = "meetups") { records ->
            assertThat(records[3].key()).isEqualTo(meetupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.MeetupRatedEvent>(records[3].value()) }
        }
    }
}