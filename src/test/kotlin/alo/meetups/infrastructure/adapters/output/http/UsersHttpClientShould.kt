package alo.meetups.infrastructure.adapters.output.http

import alo.meetups.domain.model.Email
import alo.meetups.domain.model.User
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.UserNotFound
import alo.meetups.fixtures.stubHttpEndpointForFindUserNonSucceeded
import alo.meetups.fixtures.stubHttpEndpointForFindUserNotFound
import alo.meetups.fixtures.stubHttpEndpointForFindUserSucceeded
import arrow.core.left
import arrow.core.right
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID

@Tag("integration")
class UsersHttpClientShould {

    private val usersExternalService = WireMockRule(wireMockConfig().dynamicPort()).also { it.start() }

    private val findUser = UsersHttpClient(usersExternalService.baseUrl())

    @Test
    fun `find a user`() {
        val userId = UUID.randomUUID()
        usersExternalService.stubHttpEndpointForFindUserSucceeded(userId = userId)

        val result = findUser(UserId(userId))

        assertThat(result).isEqualTo(User(UserId(userId), Email("jane.doe@gmail.com")).right())
    }

    @Test
    fun `fail when user does not exists`() {
        val userId = UUID.randomUUID()
        usersExternalService.stubHttpEndpointForFindUserNotFound(userId)

        val result = findUser(UserId(userId))

        assertThat(result).isEqualTo(UserNotFound.left())
    }

    @Test
    fun `crash when there is a non successful http response`() {
        val userId = UUID.randomUUID()
        usersExternalService.stubHttpEndpointForFindUserNonSucceeded(userId)

        assertThatThrownBy { findUser(UserId(userId)) }.isExactlyInstanceOf(HttpClientFailureException::class.java)
    }
}
