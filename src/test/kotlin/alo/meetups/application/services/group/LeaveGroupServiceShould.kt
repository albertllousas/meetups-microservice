package alo.meetups.application.services.group

import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.MemberWasNotPartOfTheGroup
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.UserNotFound
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.fixtures.GroupBuilder
import alo.meetups.fixtures.UserBuilder
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class LeaveGroupServiceShould {


    private val groupRepository = mockk<GroupRepository>(relaxed = true)

    private val findUser = mockk<FindUser>()

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val leaveGroup = LeaveGroupService(findUser, groupRepository, publishEvent)

    @Test
    fun `leave a group as a member`() {
        val memberId = UserId(UUID.randomUUID())
        val member = UserBuilder.build(memberId)
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(id = groupId, members = setOf(memberId))
        every { groupRepository.find(groupId) } returns group.right()
        every { findUser(memberId) } returns member.right()

        val result = leaveGroup(LeaveGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(Unit.right())
        verify { groupRepository.update(group.copy(members = emptySet())) }
        verify { publishEvent(GroupEvent.MemberLeft(group.copy(members = emptySet()), memberId = memberId)) }
    }

    @Test
    fun `fail leaving a group when member does not exists`() {
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(groupId)
        val memberId = UserId(UUID.randomUUID())
        every { groupRepository.find(groupId) } returns group.right()
        every { findUser(memberId) } returns UserNotFound.left()

        val result = leaveGroup(LeaveGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(UserNotFound.left())
    }

    @Test
    fun `fail leaving a group when group does not exists`() {
        val groupId = GroupId(UUID.randomUUID())
        val memberId = UserId(UUID.randomUUID())
        val member = UserBuilder.build(memberId)
        every { groupRepository.find(groupId) } returns GroupNotFound.left()
        every { findUser(memberId) } returns member.right()

        val result = leaveGroup(LeaveGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(GroupNotFound.left())
    }

    @Test
    fun `fail leaving a group when the member was not part of the group`() {
        val memberId = UserId(UUID.randomUUID())
        val member = UserBuilder.build(memberId)
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(groupId)
        every { groupRepository.find(groupId) } returns group.right()
        every { findUser(memberId) } returns member.right()

        val result = leaveGroup(LeaveGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(MemberWasNotPartOfTheGroup(memberId.value).left())
    }
}
