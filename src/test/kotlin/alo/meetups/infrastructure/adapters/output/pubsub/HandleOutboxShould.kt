package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.domain.model.meetup.MeetupType.Online
import alo.meetups.fixtures.GroupBuilder
import alo.meetups.fixtures.MeetupBuilder
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.GroupCreatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.IntegrationEvent.MeetupCreatedEvent
import alo.meetups.infrastructure.adapters.output.pubsub.MeetupDto.MeetupTypeIntegration.ONLINE
import alo.meetups.infrastructure.outbox.OutboxEvent
import alo.meetups.infrastructure.outbox.TransactionalOutbox
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class HandleOutboxShould {
    private val transactionalOutbox = mockk<TransactionalOutbox>(relaxed = true)

    private val objectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())

    private val eventId = UUID.randomUUID()

    private val generateId = { eventId }

    private val now = LocalDateTime.now()

    private val clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))

    private val handleOutbox = HandleOutbox(
        transactionalOutbox = transactionalOutbox,
        meetupEventStream = "meetup_stream",
        groupEventStream = "group_stream",
        mapper = objectMapper,
        clock = clock,
        generateId = generateId
    )

    @Test
    fun `store meetup domain events into the outbox for fue=rther reliable publishing`() {
        val meetup = MeetupBuilder.build()

        handleOutbox(MeetupEvent.MeetupCreated(meetup))

        val expectedIntegrationEvent = MeetupCreatedEvent(
            meetup = MeetupDto(
                id = meetup.id.value,
                hostedBy = meetup.hostedBy.value,
                status = MeetupDto.MeetupStatusIntegration.UPCOMING,
                topic = meetup.topic.value,
                details = meetup.details.value,
                on = meetup.on,
                groupId = meetup.groupId?.value,
                type = ONLINE,
                address = null,
                linkURL = (meetup.type as Online).link.url.toString(),
                linkName = (meetup.type as Online).link.name,
                attendees = emptyList(),
                cancelledReason = null,
                ratingStars = null,
                ratingVotes = null
            ),
            eventId = eventId,
            occurredOn = now
        )
        verify {
            transactionalOutbox.storeForReliablePublishing(
                OutboxEvent(
                    aggregateId = meetup.id.value,
                    stream = "meetup_stream",
                    payload = objectMapper.writeValueAsBytes(expectedIntegrationEvent)
                )
            )
        }
    }

    @Test
    fun `store group domain events into the outbox for fue=rther reliable publishing`() {
        val group = GroupBuilder.build()

        handleOutbox(GroupEvent.GroupCreated(group))

        val expectedIntegrationEvent = GroupCreatedEvent(
            group = GroupDto(
                group.id.value, group.title.value, group.members.map { it.value }, group.meetups.map { it.value }
            ),
            eventId = eventId,
            occurredOn = now
        )
        verify {
            transactionalOutbox.storeForReliablePublishing(
                OutboxEvent(
                    aggregateId = group.id.value,
                    stream = "group_stream",
                    payload = objectMapper.writeValueAsBytes(expectedIntegrationEvent)
                )
            )
        }
    }
}
