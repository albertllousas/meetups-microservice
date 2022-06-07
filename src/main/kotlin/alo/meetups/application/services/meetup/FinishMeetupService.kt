package alo.meetups.application.services.meetup

import alo.meetups.application.services.UseCase
import alo.meetups.domain.model.FinishMeetupError
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.Transactional
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import arrow.core.Either
import arrow.core.flatMap
import java.util.UUID

typealias FinishMeetup = UseCase<FinishMeetupRequest, FinishMeetupError, Unit>

class FinishMeetupService(
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
    private val transactional: Transactional,
) : FinishMeetup {

    override operator fun invoke(request: FinishMeetupRequest): Either<FinishMeetupError, Unit> = transactional {
        meetupRepository.find(MeetupId(request.id))
            .flatMap { it.finish() }
            .tap(meetupRepository::update)
            .map(MeetupEvent::MeetupFinished)
            .map(publishEvent::invoke)
    }
}

data class FinishMeetupRequest(val id: UUID)
