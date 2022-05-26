package alo.meetups.application.services.meetup

import alo.meetups.domain.model.MeetupEvent.MeetupFinished
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeFinished
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
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

class FinishMeetupServiceShould {

    private val meetupRepository = mockk<MeetupRepository>(relaxed = true)

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val finishMeetup = FinishMeetupService(meetupRepository, publishEvent)

    @Test
    fun `finish a meetup`() {
        val request = FinishMeetupRequest(id = UUID.randomUUID())
        val meetup = MeetupBuilder.build()
        every { meetupRepository.find(MeetupId(request.id)) } returns meetup.right()
        every { meetupRepository.update(any()) } returns Unit

        val result = finishMeetup(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { publishEvent(ofType(MeetupFinished::class)) }
    }

    @Test
    fun `fail finishing a non existent meeting`() {
        val request = FinishMeetupRequest(id = UUID.randomUUID())
        every { meetupRepository.find(MeetupId(request.id)) } returns MeetupNotFound.left()

        val result = finishMeetup(request)

        assertThat(result).isEqualTo(MeetupNotFound.left())
    }

    @Test
    fun `fail finishing a meetup that is not in an upcoming state`() {
        val request = FinishMeetupRequest(id = UUID.randomUUID())
        val meetup = MeetupBuilder.build(status = Finished(Rating.create()))
        every { meetupRepository.find(MeetupId(request.id)) } returns meetup.right()

        val result = finishMeetup(request)

        assertThat(result).isEqualTo(OnlyUpcomingMeetupsCanBeFinished.left())
    }
}
