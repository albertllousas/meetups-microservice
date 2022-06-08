package alo.meetups.fixtures

import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.meetup.Details
import alo.meetups.domain.model.meetup.Link
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupStatus
import alo.meetups.domain.model.meetup.MeetupStatus.*
import alo.meetups.domain.model.meetup.MeetupType
import alo.meetups.domain.model.meetup.Rating
import alo.meetups.domain.model.meetup.Topic
import com.github.javafaker.Faker
import java.net.URL
import java.time.ZonedDateTime
import java.util.UUID

private val faker = Faker()

object MeetupBuilder {

    fun finished(
        id: MeetupId = MeetupId(UUID.randomUUID()),
        hostedBy: UserId = UserId(UUID.randomUUID()),
        topic: Topic = Topic.reconstitute(faker.rickAndMorty().quote()),
        details: Details = Details.reconstitute(faker.lebowski().quote()),
        on: ZonedDateTime = ZonedDateTime.now(),
        groupId: GroupId? = GroupId(UUID.randomUUID()),
        type: MeetupType = MeetupType.Online(
            Link.reconstitute(name = faker.company().name(), url = "http://${faker.company().url()}")
        ),
        attendees: Set<UserId> = emptySet(),
    ) = build(
        id, hostedBy, Finished(Rating.reconstitute(3.toBigDecimal(), 6)), topic, details, on, groupId, type, attendees
    )

    fun build(
        id: MeetupId = MeetupId(UUID.randomUUID()),
        hostedBy: UserId = UserId(UUID.randomUUID()),
        status: MeetupStatus = Upcoming,
        topic: Topic = Topic.reconstitute(faker.rickAndMorty().quote()),
        details: Details = Details.reconstitute(faker.lebowski().quote()),
        on: ZonedDateTime = ZonedDateTime.now(),
        groupId: GroupId? = GroupId(UUID.randomUUID()),
        type: MeetupType = MeetupType.Online(
            Link.reconstitute(name = faker.company().name(), url = "http://${faker.company().url()}")
        ),
        attendees: Set<UserId> = emptySet(),
        aggregateVersion: Long = 0
    ) = Meetup.reconstitute(
        id = id,
        hostedBy = hostedBy,
        status = status,
        topic = topic,
        details = details,
        on = on,
        groupId = groupId,
        type = type,
        attendees = attendees,
        aggregateVersion
    )
}