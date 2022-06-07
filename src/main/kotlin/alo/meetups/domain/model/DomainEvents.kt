package alo.meetups.domain.model

import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupId

sealed interface DomainEvent

sealed class MeetupEvent: DomainEvent {

    abstract val meetup: Meetup

    data class MeetupCreated(override val meetup: Meetup): MeetupEvent()
    data class MeetupCancelled(override val meetup: Meetup): MeetupEvent()
    data class MeetupFinished(override val meetup: Meetup): MeetupEvent()
    data class AttendantAdded(override val meetup: Meetup, val newAttendant: UserId): MeetupEvent()
    data class MeetupRated(override val meetup: Meetup,val attendant: UserId, val score: Int): MeetupEvent()
}

sealed class GroupEvent: DomainEvent {

    abstract val group: Group

    data class GroupCreated(override val group: Group): GroupEvent()
    data class MeetupIncluded(override val group: Group, val meetupId: MeetupId): GroupEvent()
    data class MemberJoined(override val group: Group, val memberId: UserId): GroupEvent()
    data class MemberLeft(override val group: Group, val memberId: UserId): GroupEvent()
}

interface PublishEvent {
    operator fun invoke(domainEvent: DomainEvent)
}

interface HandleEvent {
    operator fun invoke(domainEvent: DomainEvent)
}
