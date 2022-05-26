package alo.meetups.application.services.group

import alo.meetups.domain.model.AlreadyIncluded
import alo.meetups.domain.model.GroupEvent.MeetupIncluded
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.fixtures.GroupBuilder
import alo.meetups.fixtures.MeetupBuilder
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class IncludeMeetupServiceShould {

    private val groupRepository = mockk<GroupRepository>(relaxed = true)

    private val meetupRepository = mockk<MeetupRepository>()

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val includeMeetup = IncludeMeetupService(groupRepository, meetupRepository, publishEvent)

    @Test
    fun `add an attendant to a meetup`() {
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(groupId)
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(meetupId)
        every { groupRepository.find(groupId) } returns group.right()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = includeMeetup(IncludeMeetupRequest(groupId.value, meetupId.value))

        assertThat(result).isEqualTo(Unit.right())
        verify { groupRepository.update(group.copy(meetups = setOf(meetup.id))) }
        verify { publishEvent(MeetupIncluded(group.copy(meetups = setOf(meetup.id)), meetupId)) }
    }

    @Test
    fun `fail including a meetup when meeting does not exists`() {
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(groupId)
        val meetupId = MeetupId(UUID.randomUUID())
        every { groupRepository.find(groupId) } returns group.right()
        every { meetupRepository.find(meetupId) } returns MeetupNotFound.left()

        val result = includeMeetup(IncludeMeetupRequest(groupId.value, meetupId.value))

        assertThat(result).isEqualTo(MeetupNotFound.left())
    }

    @Test
    fun `fail including a meetup when group does not exists`() {
        val groupId = GroupId(UUID.randomUUID())
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(meetupId)
        every { groupRepository.find(groupId) } returns GroupNotFound.left()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = includeMeetup(IncludeMeetupRequest(groupId.value, meetupId.value))

        assertThat(result).isEqualTo(GroupNotFound.left())
    }

    @Test
    fun `fail including a meetup when group fails for any reason`() {
        val meetupId = MeetupId(UUID.randomUUID())
        val meetup = MeetupBuilder.build(meetupId)
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(id = groupId, meetups = setOf(meetupId))
        every { groupRepository.find(groupId) } returns group.right()
        every { meetupRepository.find(meetupId) } returns meetup.right()

        val result = includeMeetup(IncludeMeetupRequest(groupId.value, meetupId.value))

        assertThat(result).isEqualTo(AlreadyIncluded(meetupId.value).left())
    }
}