package alo.meetups.component.meetup

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
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
    }
}