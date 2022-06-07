package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.AttendantAddedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.GroupCreatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupCancelledEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupCreatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupFinishedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupIncludedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupRatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MemberJoinedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MemberLeftEvent
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

/*
Integration event: A committed event that occurred in the past within a bounded context which may be interesting to other
domains, applications or third party services. Usually these definitions are imported from the scheme registry.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "event_type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MeetupCreatedEvent::class, name = "meetup_created_event"),
    JsonSubTypes.Type(value = MeetupCancelledEvent::class, name = "meetup_cancelled_event"),
    JsonSubTypes.Type(value = AttendantAddedEvent::class, name = "attendant_added_event"),
    JsonSubTypes.Type(value = MeetupFinishedEvent::class, name = "meetup_finished_event"),
    JsonSubTypes.Type(value = MeetupRatedEvent::class, name = "meetup_rated_event"),
    JsonSubTypes.Type(value = GroupCreatedEvent::class, name = "group_created_event"),
    JsonSubTypes.Type(value = MeetupIncludedEvent::class, name = "meetup_included_event"),
    JsonSubTypes.Type(value = MemberLeftEvent::class, name = "group_left_event"),
    JsonSubTypes.Type(value = MemberJoinedEvent::class, name = "group_joined_event"),
)
sealed class IntegrationEvent(
    @get:JsonProperty("event_type") val eventType: String,
    @get:JsonProperty("aggregate_type") val aggregateType: String,
) {
    @get:JsonProperty("occurred_on")
    abstract val occurredOn: LocalDateTime

    @get:JsonProperty("event_id")
    abstract val eventId: UUID

    data class MeetupCreatedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
    ) : IntegrationEvent("meetup_created_event", "meetup")

    data class MeetupCancelledEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
    ) : IntegrationEvent("meetup_cancelled_event", "meetup")

    data class AttendantAddedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("attendant_id") val attendantId: UUID,
    ) : IntegrationEvent("attendant_added_event", "meetup")

    data class MeetupFinishedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
    ) : IntegrationEvent("meetup_finished_event", "meetup")

    data class MeetupRatedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
    ) : IntegrationEvent("meetup_rated_event", "meetup")

    data class GroupCreatedEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
    ) : IntegrationEvent("group_created_event", "group")

    data class MeetupIncludedEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("meetup_id") val meetupId: UUID,
    ) : IntegrationEvent("meetup_included_event", "group")

    data class MemberLeftEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("member_id") val memberId: UUID,
    ) : IntegrationEvent("group_left_event", "group")

    data class MemberJoinedEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("member_id") val memberId: UUID,
    ) : IntegrationEvent("group_joined_event", "group")
}

data class MeetupDto(
    val id: UUID,
    val hostedBy: UUID,
    val status: MeetupStatusIntegration,
    val cancelledReason: String?,
    val ratingStars: BigDecimal?,
    val ratingVotes: Int?,
    val topic: String,
    val details: String,
    val on: ZonedDateTime,
    val groupId: UUID?,
    val type: MeetupTypeIntegration,
    val address: String?,
    val linkURL: String?,
    val linkName: String?,
    val attendees: List<UUID>,
) {
    enum class MeetupStatusIntegration {
        UPCOMING, CANCELLED, FINISHED
    }

    enum class MeetupTypeIntegration {
        ONLINE, IN_PERSON
    }
}

data class GroupDto(val id: UUID, val title: String, val members: List<UUID>, val meetups: List<UUID>)
