package alo.meetups.application.services.meetup

import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.MeetupEvent.MeetupRated
import alo.meetups.domain.model.MeetupNotFinishedYet
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.UserNotFound
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
import alo.meetups.domain.model.meetup.Rating
import alo.meetups.fixtures.MeetupBuilder
import alo.meetups.fixtures.UserBuilder
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class RateMeetupServiceShould {

    private val findUser = mockk<FindUser>()

    private val meetupRepository = mockk<MeetupRepository>(relaxed = true)

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val rateMeetup = RateMeetupService(findUser, meetupRepository, publishEvent)

    @Test
    fun `rate a meetup`() {
        val userId = UserId(UUID.randomUUID())
        val attendant = UserBuilder.build(userId)
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(id = meetupId, status = Finished(Rating.create()), attendees = setOf(userId))
        every { findUser(userId) } returns attendant.right()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = rateMeetup(RateMeetupRequest(meetupId.value, userId.value, 3))

        assertThat(result).isEqualTo(Unit.right())
        verify { meetupRepository.update(meetup.copy(status = Finished(Rating.reconstitute(3.toBigDecimal(), 1)))) }
        verify {
            publishEvent(
                MeetupRated(
                    meetup = meetup.copy(status = Finished(Rating.reconstitute(3.toBigDecimal(), 1))),
                    attendant = userId,
                    score = 3
                )
            )
        }
    }

    @Test
    fun `fail rating a meetup when user does not exists`() {
        val userId = UserId(UUID.randomUUID())
        val meetupId = MeetupId(UUID.randomUUID())
        every { findUser(userId) } returns UserNotFound.left()

        val result = rateMeetup(RateMeetupRequest(meetupId.value, userId.value, 3))

        assertThat(result).isEqualTo(UserNotFound.left())
    }

    @Test
    fun `fail rating a meetup when meeting does not exists`() {
        val userId = UserId(UUID.randomUUID())
        val attendant = UserBuilder.build(userId)
        val meetupId = MeetupId(UUID.randomUUID())
        every { findUser(userId) } returns attendant.right()
        every { meetupRepository.find(meetupId) } returns MeetupNotFound.left()


        val result = rateMeetup(RateMeetupRequest(meetupId.value, userId.value, 3))

        assertThat(result).isEqualTo(MeetupNotFound.left())
    }

    @Test
    fun `fail rating a meetup when rating fails`() {
        val userId = UserId(UUID.randomUUID())
        val attendant = UserBuilder.build(userId)
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(meetupId)
        every { findUser(userId) } returns attendant.right()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = rateMeetup(RateMeetupRequest(meetupId.value, userId.value, 7))

        assertThat(result).isEqualTo(MeetupNotFinishedYet.left())
    }
}