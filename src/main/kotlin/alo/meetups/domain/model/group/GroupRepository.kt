package alo.meetups.domain.model.group

import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupNotFound
import arrow.core.Either

interface GroupRepository {
    fun find(groupId: GroupId): Either<GroupNotFound, Group>
    fun create(group: Group) : Either<GroupAlreadyExists, Group>
    fun update(group: Group)
}
