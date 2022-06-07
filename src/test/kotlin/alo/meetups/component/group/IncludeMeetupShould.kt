package alo.meetups.component.group

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
class IncludeMeetupShould : BaseComponentTest() {

    @Test
    fun `include meetup in a group`() {
        val groupId = givenAGroupExists()
        val meetupId = givenAMeetupExists()
        given()
            .contentType(JSON)
            .body(""" { "meetup_id": "$meetupId" } """)
            .`when`()
            .post("/groups/$groupId/meetups")
            .then()
            .statusCode(201)

        kafkaConsumer.consumeAndAssertMultiple(numberOfMessages = 2, stream = "groups") { records ->
            assertThat(records[1].key()).isEqualTo(groupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.MeetupIncludedEvent>(records[1].value()) }
        }
    }
}
