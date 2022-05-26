package alo.meetups.application.services.meetup

import alo.meetups.domain.model.AttendMeetupError
import alo.meetups.domain.model.DomainError
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.MeetupEvent.AttendantAdded
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.meetup.MeetupId
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.zip
import java.time.Clock
import java.util.UUID

class AttendMeetupService(
    private val findUser: FindUser,
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
    private val clock: Clock
) {

    operator fun invoke(request: AttendMeetupRequest): Either<AttendMeetupError, Unit> =
        findUser(UserId(request.attendantId))
            .zip(meetupRepository.find(MeetupId(request.meetupId)))
            .flatMap { (attendant, meetup) -> meetup.attend(attendant, clock) }
            .tap(meetupRepository::update)
            .map { AttendantAdded(it, UserId(request.attendantId)) }
            .map { publishEvent(it) }
}

data class AttendMeetupRequest(val attendantId: UUID, val meetupId: UUID)