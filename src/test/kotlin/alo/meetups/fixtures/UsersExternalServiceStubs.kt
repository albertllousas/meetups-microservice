package alo.meetups.fixtures

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import java.util.UUID

fun WireMockServer.stubHttpEnpointForFindUserNonSucceeded(
    userId: UUID,
    responseCode: Int = 400,
    responseErrorBody: String = """{"status":400,"detail":"Some problem"}"""
): StubMapping =
    this.stubFor(
        get(urlEqualTo("/users/$userId"))
            .willReturn(status(responseCode).withBody(responseErrorBody))
    )

fun WireMockServer.stubHttpEnpointForFindUserNotFound(userId: UUID): StubMapping =
    this.stubHttpEnpointForFindUserNonSucceeded(
        userId, 404, """ {"status":404,"detail":"Account not found: $userId"} """
    )

fun WireMockServer.stubHttpEnpointForFindUserSucceeded(userId: UUID): StubMapping =
    this.stubFor(
        get(urlEqualTo("/users/$userId"))
            .willReturn(
                status(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                            {
                              "id": "$userId",
                              "email": "jane.doe@gmail.com",
                              "fullName": "Doe, Jane"
                            }
                        """
                    )
            )
    )
