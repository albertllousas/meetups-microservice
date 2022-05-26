package alo.meetups.application.services.meetup

import alo.meetups.domain.model.CancelMeetupError
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import arrow.core.Either
import arrow.core.flatMap
import java.util.UUID

class CancelMeetupService(
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
) {

    operator fun invoke(request: CancelMeetupRequest): Either<CancelMeetupError, Unit> =
        meetupRepository.find(MeetupId(request.id))
            .flatMap { it.cancel(request.reason) }
            .tap(meetupRepository::update)
            .map(MeetupEvent::MeetupCancelled)
            .map(publishEvent::invoke)
}

data class CancelMeetupRequest(val id: UUID, val reason: String)
