package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.DomainEvent
import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.GroupEvent.GroupCreated
import alo.meetups.domain.model.GroupEvent.MeetupIncluded
import alo.meetups.domain.model.GroupEvent.MemberJoined
import alo.meetups.domain.model.GroupEvent.MemberLeft
import alo.meetups.domain.model.HandleEvent
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.domain.model.MeetupEvent.AttendantAdded
import alo.meetups.domain.model.MeetupEvent.MeetupCancelled
import alo.meetups.domain.model.MeetupEvent.MeetupCreated
import alo.meetups.domain.model.MeetupEvent.MeetupFinished
import alo.meetups.domain.model.MeetupEvent.MeetupRated
import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupStatus.Cancelled
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
import alo.meetups.domain.model.meetup.MeetupStatus.Upcoming
import alo.meetups.domain.model.meetup.MeetupType.InPerson
import alo.meetups.domain.model.meetup.MeetupType.Online
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.AttendantAddedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.GroupCreatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupCancelledEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupCreatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupFinishedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupIncludedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupRatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MemberJoinedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MemberLeftEvent
import alo.meetups.infrastructure.adapters.output.pubsub.MeetupDto.MeetupStatusIntegration.CANCELLED
import alo.meetups.infrastructure.adapters.output.pubsub.MeetupDto.MeetupStatusIntegration.FINISHED
import alo.meetups.infrastructure.adapters.output.pubsub.MeetupDto.MeetupStatusIntegration.UPCOMING
import alo.meetups.infrastructure.adapters.output.pubsub.MeetupDto.MeetupTypeIntegration.IN_PERSON
import alo.meetups.infrastructure.adapters.output.pubsub.MeetupDto.MeetupTypeIntegration.ONLINE
import alo.meetups.infrastructure.outbox.OutboxEvent
import alo.meetups.infrastructure.outbox.TransactionalOutbox
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.LocalDateTime.now
import java.util.UUID

class HandleOutbox(
    private val transactionalOutbox: TransactionalOutbox,
    private val meetupEventStream: String,
    private val groupEventStream: String,
    private val mapper: ObjectMapper,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val generateId: () -> UUID = { UUID.randomUUID() },
) : HandleEvent {

    override operator fun invoke(event: DomainEvent) {
        when (event) {
            is GroupCreated -> GroupCreatedEvent(event.group.asDto(), now(clock), generateId())
            is MeetupIncluded -> MeetupIncludedEvent(event.group.asDto(), now(clock), generateId(), event.meetupId.value)
            is MemberJoined -> MemberJoinedEvent(event.group.asDto(), now(clock), generateId(), event.memberId.value)
            is MemberLeft -> MemberLeftEvent(event.group.asDto(), now(clock), generateId(), event.memberId.value)
            is AttendantAdded -> AttendantAddedEvent(event.meetup.asDto(), now(clock), generateId(), event.newAttendant.value)
            is MeetupCancelled -> MeetupCancelledEvent(event.meetup.asDto(), now(clock), generateId())
            is MeetupCreated -> MeetupCreatedEvent(event.meetup.asDto(), now(clock), generateId())
            is MeetupFinished -> MeetupFinishedEvent(event.meetup.asDto(), now(clock), generateId())
            is MeetupRated -> MeetupRatedEvent(event.meetup.asDto(), now(clock), generateId())
        }.let {
            transactionalOutbox.storeForReliablePublishing(
                OutboxEvent(
                    stream = when (event) {
                        is GroupEvent -> groupEventStream
                        is MeetupEvent -> meetupEventStream
                    },
                    payload = mapper.writeValueAsBytes(it),
                    aggregateId = when (event) {
                        is GroupEvent -> event.group.id.value
                        is MeetupEvent -> event.meetup.id.value
                    }
                )
            )
        }
    }

    private fun Meetup.asDto() =
        MeetupDto(
            id = id.value,
            hostedBy = hostedBy.value,
            status = when (status) {
                is Cancelled -> CANCELLED
                is Finished -> FINISHED
                Upcoming -> UPCOMING
            },
            cancelledReason = if (status is Cancelled) status.reason else null,
            ratingStars = if (status is Finished) status.rating.stars else null,
            ratingVotes = if (status is Finished) status.rating.votes else null,
            topic = topic.value,
            details = details.value,
            on = on,
            groupId = groupId?.value,
            type = when (type) {
                is InPerson -> IN_PERSON
                is Online -> ONLINE
            },
            address = if (type is InPerson) type.address.value else null,
            linkURL = if (type is Online) type.link.url.toString() else null,
            linkName = if (type is Online) type.link.name else null,
            attendees = attendees.map { it.value },


            )

    private fun Group.asDto() = GroupDto(id.value,title.value, members.map { it.value }, meetups.map { it.value })
}
