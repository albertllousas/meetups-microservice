package alo.meetups.application.services.group

import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.IncludeMeetupError
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.zip
import java.util.UUID

class IncludeMeetupService(
    private val groupRepository: GroupRepository,
    private val meetupRepository: MeetupRepository,
    private val publishEvent: PublishEvent,
) {

    operator fun invoke(request: IncludeMeetupRequest): Either<IncludeMeetupError, Unit> =
        groupRepository.find(GroupId(request.groupId))
            .zip(meetupRepository.find(MeetupId(request.meetupId)))
            .flatMap { (group, meetup) -> group.include(meetup) }
            .tap(groupRepository::update)
            .map { GroupEvent.MeetupIncluded(it, MeetupId(request.meetupId)) }
            .map { publishEvent(it) }
}

data class IncludeMeetupRequest(val groupId: UUID, val meetupId: UUID)
