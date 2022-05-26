package alo.meetups.application.services.group

import alo.meetups.domain.model.CreateGroupError
import alo.meetups.domain.model.DomainError
import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.group.GroupRepository
import arrow.core.Either
import arrow.core.flatMap
import java.util.UUID

class CreateGroupService(private val groupRepository: GroupRepository, private val publishEvent: PublishEvent) {

    operator fun invoke(request: CreateGroupRequest): Either<CreateGroupError, Unit> =
        Group.create(request.id, request.title)
            .flatMap(groupRepository::create)
            .map(GroupEvent::GroupCreated)
            .map(publishEvent::invoke)
}

data class CreateGroupRequest(val id: UUID, val title: String)