package alo.meetups.application.services.meetup

import alo.meetups.application.services.UseCase
import alo.meetups.application.services.meetup.CreateMeetupRequest.Type.InPerson
import alo.meetups.application.services.meetup.CreateMeetupRequest.Type.Online
import alo.meetups.domain.model.CreateMeetupError
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.MeetupEvent.MeetupCreated
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.Transactional
import alo.meetups.domain.model.User
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupRepository
import arrow.core.Either
import arrow.core.flatMap
import java.time.Clock
import java.time.ZonedDateTime
import java.util.UUID

typealias CreateMeetup = UseCase<CreateMeetupRequest, CreateMeetupError, Unit>

class CreateMeetupService(
    private val findUser: FindUser,
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
    private val transactional: Transactional,
    private val clock: Clock,
) : CreateMeetup {

    override operator fun invoke(request: CreateMeetupRequest): Either<CreateMeetupError, Unit> = transactional {
        findUser(UserId(request.hostId))
            .flatMap { host: User -> createMeetup(request, host) }
            .flatMap(meetupRepository::create)
            .map(::MeetupCreated)
            .map(publishEvent::invoke)
    }

    private fun createMeetup(request: CreateMeetupRequest, host: User) =
        with(request) {
            when (request.type) {
                is Online ->
                    Meetup.upcomingOnline(id, topic, host, details, clock, on, request.type.name, request.type.url)
                is InPerson ->
                    Meetup.upcomingInPerson(id, topic, host, details, clock, on, request.type.address)
            }
        }
}

data class CreateMeetupRequest(
    val id: UUID,
    val hostId: UUID,
    val topic: String,
    val details: String,
    val on: ZonedDateTime,
    val type: Type,
) {
    sealed class Type {
        data class Online(val name: String, val url: String) : Type()
        data class InPerson(val address: String) : Type()
    }
}
