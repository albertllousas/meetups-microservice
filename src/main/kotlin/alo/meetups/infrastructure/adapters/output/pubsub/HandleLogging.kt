package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.DomainEvent
import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.HandleEvent
import alo.meetups.domain.model.MeetupEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

class HandleLogging(
    private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()),
) : HandleEvent {

    override fun invoke(domainEvent: DomainEvent) {
        logger.info(domainEvent.logMessage())
    }

    private fun DomainEvent.logMessage(): String {
        val common = "domain-event: '${this::class.simpleName}'"
        val aggregate = when (this) {
            is MeetupEvent -> "aggregate: 'meetup', id: '${this.meetup.id.value}'"
            is GroupEvent -> "aggregate: 'group', id: '${this.group.id.value}'"
        }
        val detail = when (this) {
            is GroupEvent.MeetupIncluded -> "meetup-id: '${this.meetupId.value}'"
            is GroupEvent.MemberJoined -> "member-id: '${this.memberId.value}'"
            is GroupEvent.MemberLeft -> "member-id: '${this.memberId.value}'"
            is MeetupEvent.AttendantAdded -> "attendant-id: '${this.newAttendant.value}'"
            else -> null
        }
        return (listOf(common, aggregate) + (detail?.let { listOf(detail) }?: emptyList())).joinToString(", ")
    }
}
