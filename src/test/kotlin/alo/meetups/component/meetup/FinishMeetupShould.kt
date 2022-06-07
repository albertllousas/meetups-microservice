package alo.meetups.component.meetup

import alo.meetups.component.BaseComponentTest
import alo.meetups.fixtures.consumeAndAssert
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
import java.time.ZonedDateTime
import java.util.UUID

@Tag("Component")
@QuarkusTest
class FinishMeetupShould : BaseComponentTest() {

    @Test
    fun `finish a meetup`() {
        val meetupId = givenAMeetupExists()
        given()
            .`when`()
            .patch("/meetups/$meetupId/finish")
            .then()
            .statusCode(204)

        kafkaConsumer.consumeAndAssertMultiple(numberOfMessages = 2, stream = "meetups") { records ->
            assertThat(records[1].key()).isEqualTo(meetupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.MeetupFinishedEvent>(records[1].value()) }
        }
    }
}