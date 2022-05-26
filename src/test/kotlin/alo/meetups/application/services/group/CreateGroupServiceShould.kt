package alo.meetups.application.services.group

import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupEvent.GroupCreated
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.TooLongTitle
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.domain.model.group.Title
import alo.meetups.fixtures.GroupBuilder
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateGroupServiceShould {

    private val faker = Faker()

    private val groupRepository = mockk<GroupRepository>(relaxed = true)

    private val publishEvent = mockk<PublishEvent>(relaxed = true)

    private val createGroup = CreateGroupService(groupRepository, publishEvent)

    @Test
    fun `create an online meetup`() {
        val request = CreateGroupRequest(UUID.randomUUID(), faker.chuckNorris().fact())
        val group = GroupBuilder.build(GroupId(request.id), Title.reconstitute(request.title))
        every { groupRepository.create(any()) } returns group.right()

        val result = createGroup(request)

        assertThat(result).isEqualTo(Unit.right())
        verify { publishEvent(GroupCreated(group)) }
    }

    @Test
    fun `fail when meetup creation fails for any reason`() {
        val request = CreateGroupRequest(UUID.randomUUID(), faker.lorem().paragraph(100))

        val result = createGroup(request)

        assertThat(result).isEqualTo(TooLongTitle.left())
    }

    @Test
    fun `fail when group already exists`() {
        val request = CreateGroupRequest(UUID.randomUUID(), faker.chuckNorris().fact())
        every { groupRepository.create(any()) } returns GroupAlreadyExists.left()

        val result = createGroup(request)

        assertThat(result).isEqualTo(GroupAlreadyExists.left())
    }
}