package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.AlreadyAttendingToTheMeetup
import alo.meetups.domain.model.AttendMeetupError
import alo.meetups.domain.model.CreateMeetupError
import alo.meetups.domain.model.MeetupDateAlreadyPassed
import alo.meetups.domain.model.MeetupIsNotOpenForAttendants
import alo.meetups.domain.model.MeetupNotFinishedYet
import alo.meetups.domain.model.OnlyAttendantsCanRate
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeCancelled
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeFinished
import alo.meetups.domain.model.RateMeetupError
import alo.meetups.domain.model.User
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.meetup.MeetupStatus.Cancelled
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
import alo.meetups.domain.model.meetup.MeetupStatus.Upcoming
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.zip
import java.time.Clock
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.UUID

data class Meetup private constructor(
    val id: MeetupId,
    val hostedBy: UserId,
    val status: MeetupStatus,
    val topic: Topic,
    val details: Details,
    val on: ZonedDateTime,
    val groupId: GroupId?,
    val type: MeetupType,
    val attendees: Set<UserId>,
    val aggregateVersion: Long
) {
    companion object {

        fun upcomingOnline(
            id: UUID,
            topic: String,
            hostedBy: User,
            details: String,
            clock: Clock,
            on: ZonedDateTime,
            linkName: String,
            linkUrl: String,
        ): Either<CreateMeetupError, Meetup> =
            Link.create(linkName, linkUrl)
                .flatMap { upcoming(id, topic, hostedBy, details, clock, on, MeetupType.Online(it)) }

        fun upcomingInPerson(
            id: UUID,
            topic: String,
            hostedBy: User,
            details: String,
            clock: Clock,
            on: ZonedDateTime,
            address: String,
        ): Either<CreateMeetupError, Meetup> =
            upcoming(id, topic, hostedBy, details, clock, on, MeetupType.InPerson(Address(address)))

        private fun upcoming(
            id: UUID,
            topic: String,
            hostedBy: User,
            details: String,
            clock: Clock,
            on: ZonedDateTime,
            type: MeetupType,
        ): Either<CreateMeetupError, Meetup> =
            checkDateOnTheFuture(on, clock)
                .zip(Topic.create(topic), Details.create(details))
                { zdt: ZonedDateTime, t: Topic, d: Details ->
                    Meetup(
                        id = MeetupId(id),
                        hostedBy = hostedBy.userId,
                        status = Upcoming,
                        topic = t,
                        details = d,
                        on = zdt,
                        groupId = null,
                        type = type,
                        attendees = emptySet(),
                        aggregateVersion = 0
                    )
                }

        private fun checkDateOnTheFuture(
            on: ZonedDateTime,
            clock: Clock,
        ): Either<MeetupDateAlreadyPassed, ZonedDateTime> =
            if (on.isBefore(now(clock))) MeetupDateAlreadyPassed.left()
            else on.right()

        fun reconstitute(
            id: MeetupId,
            hostedBy: UserId,
            status: MeetupStatus,
            topic: Topic,
            details: Details,
            on: ZonedDateTime,
            groupId: GroupId?,
            type: MeetupType,
            attendees: Set<UserId>,
            aggregateVersion: Long
        ): Meetup = Meetup(id, hostedBy, status, topic, details, on, groupId, type, attendees, aggregateVersion)
    }

    fun cancel(reason: String): Either<OnlyUpcomingMeetupsCanBeCancelled, Meetup> =
        if (this.status is Upcoming) this.copy(status = Cancelled(reason = reason)).right()
        else OnlyUpcomingMeetupsCanBeCancelled.left()


    fun finish(): Either<OnlyUpcomingMeetupsCanBeFinished, Meetup> =
        if (this.status is Upcoming) this.copy(status = Finished(rating = Rating.create())).right()
        else OnlyUpcomingMeetupsCanBeFinished.left()

    fun rate(score: Int, user: User): Either<RateMeetupError, Meetup> =
        when {
            status !is Finished -> MeetupNotFinishedYet.left()
            !attendees.contains(user.userId) -> OnlyAttendantsCanRate.left()
            else -> status.rating.rate(score).map { this.copy(status = status.copy(rating = it)) }
        }

    fun attend(attendee: User, clock: Clock): Either<AttendMeetupError, Meetup> =
        when {
            status !is Upcoming -> MeetupIsNotOpenForAttendants.left()
            now(clock).isAfter(on) -> MeetupDateAlreadyPassed.left()
            attendees.contains(attendee.userId) -> AlreadyAttendingToTheMeetup(attendee.userId.value).left()
            else -> this.copy(attendees = attendees + attendee.userId).right()
        }
}

@JvmInline
value class MeetupId(val value: UUID)

sealed class MeetupStatus {
    object Upcoming : MeetupStatus()
    data class Cancelled(val reason: String) : MeetupStatus()
    data class Finished(val rating: Rating) : MeetupStatus()
}


sealed class MeetupType {
    data class InPerson(val address: Address) : MeetupType()
    data class Online(val link: Link) : MeetupType()
}

@JvmInline
value class Address(val value: String)
