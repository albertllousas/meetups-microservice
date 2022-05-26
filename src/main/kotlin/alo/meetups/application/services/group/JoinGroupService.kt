package alo.meetups.application.services.group

import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.JoinGroupError
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.GroupRepository
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.zip
import java.util.UUID

class JoinGroupService(
    private val findUser: FindUser,
    private val groupRepository: GroupRepository,
    private val publishEvent: PublishEvent,
) {

    operator fun invoke(request: JoinGroupRequest): Either<JoinGroupError, Unit> =
        findUser(UserId(request.newMemberId))
            .zip(groupRepository.find(GroupId(request.groupId)))
            .flatMap { (newMember, group) -> group.join(newMember) }
            .tap(groupRepository::update)
            .map { GroupEvent.MemberJoined(it, UserId(request.newMemberId)) }
            .map { publishEvent(it) }
}

data class JoinGroupRequest(val groupId: UUID, val newMemberId: UUID)
