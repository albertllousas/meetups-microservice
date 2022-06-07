package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.InvalidLinkURL
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.Title
import alo.meetups.domain.model.meetup.Details
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupStatus
import alo.meetups.domain.model.meetup.MeetupType
import alo.meetups.domain.model.meetup.Topic
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.*
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

/*
Integration event: A committed event that occurred in the past within a bounded context which may be interesting to other
domains, applications or third party services. Usually these definitions are imported from the scheme registry.
 */

sealed class IntegrationEvent(
    @get:JsonProperty("event_type") val eventType: String,
    @get:JsonProperty("aggregate_type") val aggregateType: String
) {
    @get:JsonProperty("occurred_on") abstract val occurredOn: LocalDateTime
    @get:JsonProperty("event_id") abstract val eventId: UUID

    data class MeetupCreatedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID
    ) : IntegrationEvent("meetup_created_event","meetup_")

    data class MeetupCancelledEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID
    ) : IntegrationEvent("meetup_cancelled_event","meetup_")

    data class AttendantAddedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("attendant_id") val attendantId: UUID,
    ) : IntegrationEvent("attendant_added_event","meetup_")

    data class MeetupFinishedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID
    ) : IntegrationEvent("meetup_finished_event","meetup_")

    data class MeetupRatedEvent(
        val meetup: MeetupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID
    ) : IntegrationEvent("meetup_rated_event","meetup_")

    data class GroupCreatedEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID
    ) : IntegrationEvent("group_created_event","group")

    data class MeetupIncludedEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("meetup_id") val meetupId: UUID,
    ) : IntegrationEvent("meetup_included_event","group")

    data class MemberLeftEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("member_id") val memberId: UUID,
    ) : IntegrationEvent("group_left_event","group")

    data class MemberJoinedEvent(
        val group: GroupDto,
        override val occurredOn: LocalDateTime,
        override val eventId: UUID,
        @get:JsonProperty("member_id") val memberId: UUID,
    ) : IntegrationEvent("group_joined_event","group")
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
    val attendees: List<UUID>
) {
    enum class MeetupStatusIntegration {
        UPCOMING, CANCELLED, FINISHED
    }
    enum class MeetupTypeIntegration {
        ONLINE, IN_PERSON
    }
}

data class GroupDto(val id: UUID, val title: String, val members: List<UUID>, val meetups: List<UUID>)
