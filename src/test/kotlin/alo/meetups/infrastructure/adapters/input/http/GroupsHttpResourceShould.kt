package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.group.CreateGroupRequest
import alo.meetups.application.services.group.CreateGroupService
import alo.meetups.application.services.group.IncludeMeetupRequest
import alo.meetups.application.services.group.IncludeMeetupService
import alo.meetups.application.services.group.JoinGroupRequest
import alo.meetups.application.services.group.JoinGroupService
import alo.meetups.application.services.group.LeaveGroupRequest
import alo.meetups.application.services.group.LeaveGroupService
import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupNotFound
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID

/**
 * Since quarkus is not providing a way to do integration test on controllers/resources in isolation (@QuarkusTest loads
 * the whole app context), unit test are performed instead.
 */
class GroupsHttpResourceShould {

    private val faker = Faker()

    private val createGroup = mockk<CreateGroupService>()

    private val includeMeetup = mockk<IncludeMeetupService>()

    private val joinGroup = mockk<JoinGroupService>()

    private val leaveGroup = mockk<LeaveGroupService>()

    private val groupsHttpResource = GroupsHttpResource(
        createGroup, includeMeetup, joinGroup, leaveGroup
    )

    @Nested
    inner class Creating {

        @Test
        fun `should create a group`() {
            val groupId = UUID.randomUUID()
            val title = faker.beer().name()
            every { createGroup(CreateGroupRequest(groupId, title)) } returns Unit.right()

            val response = groupsHttpResource.create(CreateGroupHttpRequest(groupId, title))

            assertThat(response.status).isEqualTo(201)
            assertThat(response.headers).containsEntry("Location", listOf(URI("/groups/$groupId")))
        }

        @Test
        fun `should fail if creation fails for any reason`() {
            every { createGroup(any()) } returns GroupAlreadyExists.left()

            val response = groupsHttpResource.create(CreateGroupHttpRequest(UUID.randomUUID(), faker.beer().name()))

            assertThat(response.status).isNotEqualTo(201)
        }
    }

    @Nested
    inner class IncludingMeetups {

        @Test
        fun `should include a meetup in a group`() {
            val groupId = UUID.randomUUID()
            val meetupId = UUID.randomUUID()
            every { includeMeetup(IncludeMeetupRequest(groupId, meetupId)) } returns Unit.right()

            val response = groupsHttpResource.include(groupId, IncludeMeetupHttpRequest(meetupId))

            assertThat(response.status).isEqualTo(201)
            assertThat(response.headers).containsEntry("Location", listOf(URI("/groups/$groupId")))
        }

        @Test
        fun `should fail if including a meetup in a group fails for any reason`() {
            every { includeMeetup(any()) } returns GroupNotFound.left()

            val response = groupsHttpResource.include(UUID.randomUUID(), IncludeMeetupHttpRequest(UUID.randomUUID()))

            assertThat(response.status).isNotEqualTo(201)
        }
    }

    @Nested
    inner class JoiningAGroup {

        @Test
        fun `should join a group`() {
            val groupId = UUID.randomUUID()
            val memberId = UUID.randomUUID()
            every { joinGroup(JoinGroupRequest(groupId, memberId)) } returns Unit.right()

            val response = groupsHttpResource.join(groupId, JoinGroupHttpRequest(memberId))

            assertThat(response.status).isEqualTo(201)
            assertThat(response.headers).containsEntry("Location", listOf(URI("/groups/$groupId")))
        }

        @Test
        fun `should fail if joining a group as a member fails for any reason`() {
            every { joinGroup(any()) } returns GroupNotFound.left()

            val response = groupsHttpResource.join(UUID.randomUUID(), JoinGroupHttpRequest(UUID.randomUUID()))

            assertThat(response.status).isNotEqualTo(201)
        }
    }

    @Nested
    inner class LeavingAGroup {

        @Test
        fun `should leave a group as a member`() {
            val groupId = UUID.randomUUID()
            val memberId = UUID.randomUUID()
            every { leaveGroup(LeaveGroupRequest(groupId, memberId)) } returns Unit.right()

            val response = groupsHttpResource.leave(groupId, memberId)

            assertThat(response.status).isEqualTo(204)
        }

        @Test
        fun `should fail if joining a group as a member fails for any reason`() {
            every { leaveGroup(any()) } returns GroupNotFound.left()

            val response = groupsHttpResource.leave(UUID.randomUUID(), UUID.randomUUID())

            assertThat(response.status).isNotEqualTo(204)
        }
    }
}