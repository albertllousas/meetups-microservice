package alo.meetups.domain.model

import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupId

sealed interface DomainEvent

sealed class MeetupEvent: DomainEvent {
    data class MeetupCreated(val meetup: Meetup): MeetupEvent()
    data class MeetupCancelled(val meetup: Meetup): MeetupEvent()
    data class MeetupFinished(val meetup: Meetup): MeetupEvent()
    data class AttendantAdded(val meetup: Meetup, val newAttendant: UserId): MeetupEvent()
    data class MeetupRated(val meetup: Meetup,val attendant: UserId, val score: Int): MeetupEvent()
}

sealed class GroupEvent: DomainEvent {
    data class GroupCreated(val group: Group): MeetupEvent()
    data class MeetupIncluded(val group: Group, val meetupId: MeetupId): MeetupEvent()
    data class MemberJoined(val group: Group, val memberId: UserId): MeetupEvent()
    data class MemberLeft(val group: Group, val memberId: UserId): MeetupEvent()
}

interface PublishEvent {
    operator fun invoke(domainEvent: DomainEvent)
}