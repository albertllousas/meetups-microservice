package alo.meetups.fixtures

import alo.meetups.domain.model.Email
import alo.meetups.domain.model.User
import alo.meetups.domain.model.UserId
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

object UserBuilder {

    fun build(
        id: UserId = UserId(UUID.randomUUID()),
        email: Email = Email(faker.internet().emailAddress()),
    ) = User(id, email)
}