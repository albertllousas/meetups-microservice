package alo.meetups.infrastructure.adapters.output.http

import alo.meetups.domain.model.Email
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.User
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.UserNotFound
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.Result.Success
import java.util.UUID

class UsersHttpClient(
    private val baseUrl: String,
    private val config: ExecutionConfig = ExecutionConfig(),
) : FindUser {

    @Throws(HttpClientFailureException::class)
    override fun invoke(userId: UserId): Either<UserNotFound, User> =
        Fuel.get("$baseUrl/users/${userId.value}")
            .timeout(config.connectTimeoutMillis)
            .timeoutRead(config.readTimeoutMillis)
            .responseObject<UserHttpDto>()
            .let { (_, response, result) ->
                when {
                    result is Success -> result.get().let { User(UserId(it.id), Email(it.email)) }.right()
                    response.statusCode == 404 -> UserNotFound.left()
                    else -> throw HttpClientFailureException(
                        httpClient = "users-client",
                        path = "$baseUrl/users/${userId.value}",
                        method = "GET",
                        errorBody = if (result is Result.Failure) result.error.errorData.decodeToString() else null,
                        httpStatus = response.statusCode
                    )
                }
            }
}

data class ExecutionConfig(val connectTimeoutMillis: Int = 3000, val readTimeoutMillis: Int = 3000)

data class UserHttpDto(val id: UUID, val email: String, val fullName: String)
