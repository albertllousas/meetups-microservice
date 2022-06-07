package alo.meetups.fixtures

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import java.util.UUID

fun WireMockServer.stubHttpEndpointForFindUserNonSucceeded(
    userId: UUID,
    responseCode: Int = 400,
    responseErrorBody: String = """{"status":400,"detail":"Some problem"}""",
): StubMapping =
    this.stubFor(
        get(urlEqualTo("/users/$userId"))
            .willReturn(status(responseCode).withBody(responseErrorBody))
    )

fun WireMockServer.stubHttpEndpointForFindUserNotFound(userId: UUID): StubMapping =
    this.stubHttpEndpointForFindUserNonSucceeded(
        userId, 404, """ {"status":404,"detail":"Account not found: $userId"} """
    )

fun WireMockServer.stubHttpEndpointForFindUserSucceeded(userId: UUID? = null): StubMapping =
    this.stubFor(
        get(userId?.let { urlEqualTo("/users/$userId") } ?: urlPathMatching("/users/.*"))
            .willReturn(
                status(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                            {
                              "id": "${userId ?: UUID.randomUUID()}",
                              "email": "jane.doe@gmail.com",
                              "fullName": "Doe, Jane"
                            }
                        """
                    )
            )
    )
