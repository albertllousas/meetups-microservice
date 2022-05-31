package alo.meetups.infrastructure.adapters.output.db

import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.meetup.Address
import alo.meetups.domain.model.meetup.Details
import alo.meetups.domain.model.meetup.Link
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.domain.model.meetup.MeetupStatus.Cancelled
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
import alo.meetups.domain.model.meetup.MeetupStatus.Upcoming
import alo.meetups.domain.model.meetup.MeetupType.InPerson
import alo.meetups.domain.model.meetup.MeetupType.Online
import alo.meetups.domain.model.meetup.Rating
import alo.meetups.domain.model.meetup.Topic
import alo.meetups.infrastructure.adapters.toEither
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import java.sql.ResultSet
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.UUID

class PostgresMeetupRepository(private val jdbi: Jdbi) : MeetupRepository {

    override fun find(meetupId: MeetupId): Either<MeetupNotFound, Meetup> =
        jdbi.open().use {
            it.createQuery("select * from meetups where id = :id")
                .bind("id", meetupId.value)
                .map { rs, _ -> rs.asMeetup() }
                .findOne()
                .toEither(ifEmpty = { MeetupNotFound })
        }

    override fun create(meetup: Meetup): Either<MeetupAlreadyExists, Meetup> = try {
        jdbi.open().use { handle ->
            val params = meetup.asSQLParamsMap()
            handle.execute(
                """ INSERT INTO Meetups (${params.keys.joinToString(",")}) 
                    VALUES (${(1..params.size).joinToString(",") { "?" }}) """,
                *params.values.toTypedArray()
            )
        }.let { meetup.right() }
    } catch (e: UnableToExecuteStatementException) {
        if (e.message?.let { it.contains("duplicate key") && it.contains("pk_meetup") } == true)
            MeetupAlreadyExists.left()
        else throw e
    }

    override fun update(meetup: Meetup) {
        jdbi.open().use { handle ->
            val params = meetup.asSQLParamsMap()
            handle.execute(
                """ UPDATE meetups SET ${params.map { (k, _) -> "$k = ?" }.joinToString(",")} WHERE id = ? """,
                *(params.values.toTypedArray() + meetup.id.value)
            )
        }
    }
}

private fun Meetup.asSQLParamsMap() = linkedMapOf(
    "id" to id.value,
    "topic" to topic.value,
    "details" to details.value,
    "hosted_by" to hostedBy.value,
    "on_date" to on,
    "group_id" to groupId?.value,
    "attendees" to attendees.map { it.value }.toTypedArray(),
    "meetup_type" to when (type) {
        is InPerson -> "IN_PERSON"
        is Online -> "ONLINE"
    },
    "link_name" to if (type is Online) type.link.name else null,
    "link_url" to if (type is Online) type.link.url.toExternalForm() else null,
    "address" to if (type is InPerson) type.address.value else null,
    "status" to when (status) {
        is Upcoming -> "UPCOMING"
        is Cancelled -> "CANCELLED"
        is Finished -> "FINISHED"
    },
    "cancel_reason" to if (status is Cancelled) status.reason else null,
    "rating_stars" to if (status is Finished) status.rating.stars else null,
    "rating_votes" to if (status is Finished) status.rating.votes else null,
)

private fun ResultSet.asMeetup() =
    Meetup.reconstitute(
        id = MeetupId(UUID.fromString(getString("id"))),
        hostedBy = UserId(UUID.fromString(getString("hosted_by"))),
        topic = Topic.reconstitute(getString("topic")),
        details = Details.reconstitute(getString("details")),
        on = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getTimestamp("on_date").time), UTC),
        groupId = getString("group_id")?.let { GroupId(UUID.fromString(it)) },
        type = this.asMeetupType(),
        attendees = getArray("attendees")?.let {
            (it.array as Array<UUID>).map(::UserId).toSet()
        } ?: emptySet(),
        status = this.asMeetupStatus()
    )

private fun ResultSet.asMeetupStatus() = when (getString("status")) {
    "FINISHED" -> Finished(
        Rating.reconstitute(getBigDecimal("rating_stars"), getInt("rating_votes"))
    )
    "CANCELLED" -> Cancelled(getString("cancel_reason"))
    else -> Upcoming
}

private fun ResultSet.asMeetupType() = when (getString("meetup_type")) {
    "ONLINE" -> Online(Link.reconstitute(getString("link_name"), getString("link_url")))
    else -> InPerson(Address(getString("address")))
}
