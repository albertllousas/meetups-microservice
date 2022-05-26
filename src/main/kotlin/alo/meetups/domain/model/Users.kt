package alo.meetups.domain.model

import arrow.core.Either
import java.util.UUID

interface FindUser {
    operator fun invoke(userId: UserId): Either<UserNotFound, User>
}


data class User(val userId: UserId, val email: Email)

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class Email(val value: String)


