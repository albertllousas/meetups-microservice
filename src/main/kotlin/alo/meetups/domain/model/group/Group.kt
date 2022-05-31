package alo.meetups.domain.model.group

import alo.meetups.domain.model.AlreadyIncluded
import alo.meetups.domain.model.AlreadyJoined
import alo.meetups.domain.model.MemberWasNotPartOfTheGroup
import alo.meetups.domain.model.TooLongTitle
import alo.meetups.domain.model.User
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupId
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.util.UUID

data class Group(
    val id: GroupId,
    val title: Title,
    val members: Set<UserId>,
    val meetups: Set<MeetupId>,
) {
    companion object {
        fun create(id: UUID, title: String): Either<TooLongTitle, Group> =
            Title.create(title)
                .map { Group(GroupId(id), it, emptySet(), emptySet()) }

        fun reconstitute(groupId: GroupId, title: Title, members: Set<UserId>, meetups: Set<MeetupId>): Group =
            Group(groupId, title, members, meetups)
    }

    fun join(newMember: User): Either<AlreadyJoined, Group> =
        if (!members.contains(newMember.userId)) this.copy(members = members + newMember.userId).right()
        else AlreadyJoined(newMember.userId.value).left()

    fun include(meetup: Meetup): Either<AlreadyIncluded, Group> =
        if (!meetups.contains(meetup.id)) this.copy(meetups = meetups + meetup.id).right()
        else AlreadyIncluded(meetup.id.value).left()

    fun leave(member: User): Either<MemberWasNotPartOfTheGroup, Group> =
        if (members.contains(member.userId)) this.copy(members = members - member.userId).right()
        else MemberWasNotPartOfTheGroup(member.userId.value).left()
}

@JvmInline
value class GroupId(val value: UUID)
