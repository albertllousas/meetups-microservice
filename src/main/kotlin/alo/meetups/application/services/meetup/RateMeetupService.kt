package alo.meetups.application.services.meetup

import alo.meetups.application.services.UseCase
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.MeetupEvent.MeetupRated
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.RateMeetupError
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.zip
import java.util.UUID

typealias RateMeetup = UseCase<RateMeetupRequest, RateMeetupError, Unit>

class RateMeetupService(
    private val findUser: FindUser,
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
): RateMeetup {

    override operator fun invoke(request: RateMeetupRequest): Either<RateMeetupError, Unit> =
        findUser(UserId(request.attendantId))
            .zip(meetupRepository.find(MeetupId(request.meetupId)))
            .flatMap { (attendant, meetup) -> meetup.rate(request.score, attendant) }
            .tap(meetupRepository::update)
            .map { MeetupRated(it, UserId(request.attendantId), request.score) }
            .map { publishEvent(it) }
}

data class RateMeetupRequest(val meetupId: UUID, val attendantId: UUID, val score: Int)