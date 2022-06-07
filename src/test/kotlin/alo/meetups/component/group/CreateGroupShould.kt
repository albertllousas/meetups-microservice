package alo.meetups.component.group

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
class CreateGroupShould : BaseComponentTest() {

    @Test
    fun `create a group`() {
        val groupId = UUID.randomUUID()
        given()
            .contentType(JSON)
            .body("""
               {
                    "id": "$groupId",
                    "title": "Hiking"
               }
            """)
            .`when`()
            .post("/groups")
            .then()
            .statusCode(201)

        kafkaConsumer.consumeAndAssert(stream = "groups") { record ->
            assertThat(record.key()).isEqualTo(groupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.GroupCreatedEvent>(record.value()) }
        }
    }
}
