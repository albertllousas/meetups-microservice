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
class JoinGroupShould : BaseComponentTest() {

    @Test
    fun `join a group as a member`() {
        val groupId = givenAGroupExists()
        given()
            .contentType(JSON)
            .body(""" { "member_id": "${UUID.randomUUID()}" } """)
            .`when`()
            .post("/groups/$groupId/members")
            .then()
            .statusCode(201)

        kafkaConsumer.consumeAndAssertMultiple(numberOfMessages = 2, stream = "groups") { records ->
            assertThat(records[1].key()).isEqualTo(groupId.toString())
            assertDoesNotThrow { mapper.readValue<IntegrationEvent.MemberJoinedEvent>(records[1].value()) }
        }
    }
}
