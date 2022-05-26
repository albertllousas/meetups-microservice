package alo.meetups.application.services.meetup

import alo.meetups.domain.model.MeetupEvent
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeCancelled
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.domain.model.meetup.MeetupStatus
import alo.meetups.domain.model.meetup.Rating
import alo.meetups.fixtures.MeetupBuilder
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class CancelMeetupServiceShould {

    private val meetupRepository = mockk<MeetupRepository>(relaxed = true)

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val cancelMeetup = CancelMeetupService(meetupRepository, publishEvent)

    @Test
    fun `cancel a meetup`() {
        val request = CancelMeetupRequest(
            id = UUID.randomUUID(),
            reason = "Covid-19"
        )
        val meetup = MeetupBuilder.build()
        every { meetupRepository.find(MeetupId(request.id)) } returns meetup.right()
        every { meetupRepository.update(any()) } returns Unit

        val result = cancelMeetup(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { meetupRepository.update(any()) }
        verify { publishEvent(ofType(MeetupEvent.MeetupCancelled::class)) }
    }

    @Test
    fun `fail cancelling a non existent meeting`() {
        val request = CancelMeetupRequest(
            id = UUID.randomUUID(),
            reason = "Covid-19"
        )
        every { meetupRepository.find(MeetupId(request.id)) } returns MeetupNotFound.left()

        val result = cancelMeetup(request)

        assertThat(result).isEqualTo(MeetupNotFound.left())
    }

    @Test
    fun `fail cancelling a meetup that is not in an upcoming state`() {
        val request = CancelMeetupRequest(
            id = UUID.randomUUID(),
            reason = "Covid-19"
        )
        val meetup = MeetupBuilder.build(status = MeetupStatus.Finished(Rating.create()))
        every { meetupRepository.find(MeetupId(request.id)) } returns meetup.right()

        val result = cancelMeetup(request)

        assertThat(result).isEqualTo(OnlyUpcomingMeetupsCanBeCancelled.left())
    }
}
