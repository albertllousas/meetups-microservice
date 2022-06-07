package alo.meetups.application.services.meetup

import alo.meetups.application.services.UseCase
import alo.meetups.domain.model.CancelMeetupError
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.Transactional
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import arrow.core.Either
import arrow.core.flatMap
import java.util.UUID

typealias CancelMeetup = UseCase<CancelMeetupRequest, CancelMeetupError, Unit>

class CancelMeetupService(
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
    private val transactional: Transactional,
) : CancelMeetup {

    override operator fun invoke(request: CancelMeetupRequest): Either<CancelMeetupError, Unit> = transactional {
        meetupRepository.find(MeetupId(request.id))
            .flatMap { it.cancel(request.reason) }
            .tap(meetupRepository::update)
            .map(MeetupEvent::MeetupCancelled)
            .map(publishEvent::invoke)
    }
}

data class CancelMeetupRequest(val id: UUID, val reason: String)
