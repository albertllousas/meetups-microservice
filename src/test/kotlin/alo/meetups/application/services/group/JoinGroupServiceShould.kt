package alo.meetups.application.services.group

import alo.meetups.domain.model.AlreadyJoined
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.GroupNotFound
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

class JoinGroupServiceShould {

    private val groupRepository = mockk<GroupRepository>(relaxed = true)

    private val findUser = mockk<FindUser>()

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val joinGroup = JoinGroupService(findUser, groupRepository, publishEvent)

    @Test
    fun `join a group as a new member`() {
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(groupId)
        val memberId = UserId(UUID.randomUUID())
        val member = UserBuilder.build(memberId)
        every { groupRepository.find(groupId) } returns group.right()
        every { findUser(memberId) } returns member.right()

        val result = joinGroup(JoinGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(Unit.right())
        verify { groupRepository.update(group.copy(members = setOf(memberId))) }
        verify { publishEvent(GroupEvent.MemberJoined(group.copy(members = setOf(memberId)), memberId = memberId)) }
    }

    @Test
    fun `fail joining a group when member does not exists`() {
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(groupId)
        val memberId = UserId(UUID.randomUUID())
        every { groupRepository.find(groupId) } returns group.right()
        every { findUser(memberId) } returns UserNotFound.left()

        val result = joinGroup(JoinGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(UserNotFound.left())
    }

    @Test
    fun `fail joining a group when group does not exists`() {
        val groupId = GroupId(UUID.randomUUID())
        val memberId = UserId(UUID.randomUUID())
        val member = UserBuilder.build(memberId)
        every { groupRepository.find(groupId) } returns GroupNotFound.left()
        every { findUser(memberId) } returns member.right()

        val result = joinGroup(JoinGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(GroupNotFound.left())
    }

    @Test
    fun `fail joining a group when group can not be joint`() {
        val memberId = UserId(UUID.randomUUID())
        val member = UserBuilder.build(memberId)
        val groupId = GroupId(UUID.randomUUID())
        val group = GroupBuilder.build(id = groupId, members = setOf(memberId))
        every { groupRepository.find(groupId) } returns group.right()
        every { findUser(memberId) } returns member.right()

        val result = joinGroup(JoinGroupRequest(groupId.value, memberId.value))

        assertThat(result).isEqualTo(AlreadyJoined(memberId.value).left())
    }
}
