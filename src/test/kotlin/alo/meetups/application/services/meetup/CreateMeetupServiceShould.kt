package alo.meetups.application.services.meetup

import alo.meetups.application.services.meetup.CreateMeetupRequest.Type.InPerson
import alo.meetups.application.services.meetup.CreateMeetupRequest.Type.Online
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupEvent.MeetupCreated
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.TooLongTopic
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.UserNotFound
import alo.meetups.domain.model.meetup.Address
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.domain.model.meetup.MeetupType
import alo.meetups.fixtures.MeetupBuilder
import alo.meetups.fixtures.UserBuilder
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class CreateMeetupServiceShould {

    private val faker = Faker()

    private val findUser = mockk<FindUser>()

    private val meetupRepository = mockk<MeetupRepository>(relaxed = true)

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val clock = Clock.fixed(Instant.parse("2018-08-19T16:45:42.00Z"), ZoneId.of("UTC"))

    private val createOnlineMeetup = CreateMeetupService(
        findUser, meetupRepository, publishEvent, clock
    )

    @Test
    fun `create an online meetup`() {
        val request = CreateMeetupRequest(
            id = UUID.randomUUID(),
            hostId = UUID.randomUUID(),
            topic = faker.chuckNorris().fact(),
            details = faker.lorem().sentence(),
            on = ZonedDateTime.now(clock).plusDays(1),
            type = Online(name = faker.company().name(), url = "https://${faker.company().url()}")
        )
        val host = UserBuilder.build(id = UserId(request.hostId))
        val meetup = MeetupBuilder.build()
        every { findUser(UserId(request.hostId)) } returns host.right()
        every { meetupRepository.create(any()) } returns meetup.right()

        val result = createOnlineMeetup(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { publishEvent(ofType(MeetupCreated::class)) }
    }

    @Test
    fun `create an in person meetup`() {
        val request = CreateMeetupRequest(
            id = UUID.randomUUID(),
            hostId = UUID.randomUUID(),
            topic = faker.chuckNorris().fact(),
            details = faker.lorem().sentence(),
            on = ZonedDateTime.now(clock).plusDays(1),
            type = InPerson(address = faker.address().fullAddress())
        )
        val host = UserBuilder.build(id = UserId(request.hostId))
        val meetup = MeetupBuilder.build(type = MeetupType.InPerson(Address(faker.address().fullAddress())))
        every { findUser(UserId(request.hostId)) } returns host.right()
        every { meetupRepository.create(any()) } returns meetup.right()

        val result = createOnlineMeetup(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { publishEvent(ofType(MeetupCreated::class)) }
    }

    @Test
    fun `fail when host is not found`() {
        val request = CreateMeetupRequest(
            id = UUID.randomUUID(),
            hostId = UUID.randomUUID(),
            topic = faker.chuckNorris().fact(),
            details = faker.lorem().sentence(),
            on = ZonedDateTime.now(clock).plusDays(1),
            type = Online(name = faker.company().name(), url = "https://${faker.company().url()}")
        )
        every { findUser(UserId(request.hostId)) } returns UserNotFound.left()

        val result = createOnlineMeetup(request)

        assertThat(result).isEqualTo(UserNotFound.left())
    }

    @Test
    fun `fail when meetup creation fails for any reason`() {
        val request = CreateMeetupRequest(
            id = UUID.randomUUID(),
            hostId = UUID.randomUUID(),
            topic = faker.lorem().paragraph(1000),
            details = faker.lorem().sentence(),
            on = ZonedDateTime.now(clock).plusDays(1),
            type = Online(name = faker.company().name(), url = "https://${faker.company().url()}")
        )
        val host = UserBuilder.build(id = UserId(request.hostId))
        every { findUser(UserId(request.hostId)) } returns host.right()

        val result = createOnlineMeetup(request)

        assertThat(result).isEqualTo(TooLongTopic.left())
    }

    @Test
    fun `fail when meeting already exists`() {
        val request = CreateMeetupRequest(
            id = UUID.randomUUID(),
            hostId = UUID.randomUUID(),
            topic = faker.chuckNorris().fact(),
            details = faker.lorem().sentence(3),
            on = ZonedDateTime.now(clock).plusDays(1),
            type = Online(name = faker.company().name(), url = "https://${faker.company().url()}")
        )
        val host = UserBuilder.build(id = UserId(request.hostId))
        every { findUser(UserId(request.hostId)) } returns host.right()
        every { meetupRepository.create(any()) } returns MeetupAlreadyExists.left()

        val result = createOnlineMeetup(request)

        assertThat(result).isEqualTo(MeetupAlreadyExists.left())
    }
}