package alo.meetups.application.services.meetup

import alo.meetups.domain.model.AlreadyAttendingToTheMeetup
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.MeetupEvent.AttendantAdded
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.Transactional
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.UserNotFound
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.fixtures.MeetupBuilder
import alo.meetups.fixtures.TransactionalForTesting
import alo.meetups.fixtures.UserBuilder
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class AttendMeetupServiceShould {

    private val findUser = mockk<FindUser>()

    private val meetupRepository = mockk<MeetupRepository>(relaxed = true)

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val clock = Clock.fixed(Instant.parse("2018-08-19T16:45:42.00Z"), ZoneId.of("UTC"))

    private val transactional = spyk<Transactional>(TransactionalForTesting())

    private val attendMeetup = AttendMeetupService(findUser, meetupRepository, publishEvent, transactional, clock)

    @Test
    fun `add an attendant to a meetup`() {
        val userId = UserId(UUID.randomUUID())
        val attendant = UserBuilder.build(userId)
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(meetupId)
        every { findUser(userId) } returns attendant.right()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = attendMeetup(AttendMeetupRequest(userId.value, meetupId.value))

        assertThat(result).isEqualTo(Unit.right())
        verify {
            meetupRepository.update(meetup.copy(attendees = setOf(attendant.userId)))
            publishEvent(AttendantAdded(meetup.copy(attendees = setOf(attendant.userId)), userId))
            transactional(any())
        }
    }

    @Test
    fun `fail adding an attendant to a meetup when user does not exists`() {
        val userId = UserId(UUID.randomUUID())
        val meetupId = MeetupId(UUID.randomUUID())
        every { findUser(userId) } returns UserNotFound.left()

        val result = attendMeetup(AttendMeetupRequest(userId.value, meetupId.value))

        assertThat(result).isEqualTo(UserNotFound.left())
    }

    @Test
    fun `fail adding an attendant to a meetup when meeting does not exists`() {
        val userId = UserId(UUID.randomUUID())
        val attendant = UserBuilder.build(userId)
        val meetupId = MeetupId(UUID.randomUUID())
        every { findUser(userId) } returns attendant.right()
        every { meetupRepository.find(meetupId) } returns MeetupNotFound.left()


        val result = attendMeetup(AttendMeetupRequest(userId.value, meetupId.value))

        assertThat(result).isEqualTo(MeetupNotFound.left())
    }

    @Test
    fun `fail adding an attendant to a meetup when the meetup does not accept the attendant gor any reason`() {
        val userId = UserId(UUID.randomUUID())
        val attendant = UserBuilder.build(userId)
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(id = meetupId, attendees = setOf(attendant.userId))
        every { findUser(userId) } returns attendant.right()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = attendMeetup(AttendMeetupRequest(userId.value, meetupId.value))

        assertThat(result).isEqualTo(AlreadyAttendingToTheMeetup(attendant.userId.value).left())
    }
}