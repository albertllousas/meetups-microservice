package alo.meetups.domain.model.group

import alo.meetups.domain.model.AlreadyIncluded
import alo.meetups.domain.model.AlreadyJoined
import alo.meetups.domain.model.MemberWasNotPartOfTheGroup
import alo.meetups.domain.model.TooLongTitle
import alo.meetups.fixtures.GroupBuilder
import alo.meetups.fixtures.MeetupBuilder
import alo.meetups.fixtures.UserBuilder
import arrow.core.left
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class GroupShould {

    @Nested
    inner class CreateAGroup {

        @Test
        fun `create a group`() {
            val id = UUID.randomUUID()

            val group = Group.create(id = id, title = "Cupcakes")

            assertThat(group).isEqualTo(
                Group.reconstitute(
                    groupId = GroupId(id),
                    title = Title.reconstitute("Cupcakes"),
                    meetups = emptySet(),
                    members = emptySet(),
                    aggregateVersion = 0
                ).right()
            )
        }

        @Test
        fun `fail creating a group when title fails`() {
            val id = UUID.randomUUID()

            val group = Group.create(id = id, title = (1..500).map { it.toString() }.joinToString { "" })

            assertThat(group).isEqualTo(TooLongTitle.left())
        }
    }

    @Nested
    inner class JoinAGroup {

        @Test
        fun `join a group as a member`() {
            val group = GroupBuilder.build()
            val newMember = UserBuilder.build()

            val result = group.join(newMember)

            assertThat(result).isEqualTo(group.copy(members = group.members + newMember.userId).right())
        }

        @Test
        fun `fail joining a group when the member already joined before`() {
            val newMember = UserBuilder.build()
            val group = GroupBuilder.build(members = setOf(newMember.userId))

            val result = group.join(newMember)

            assertThat(result).isEqualTo(AlreadyJoined(member = newMember.userId.value).left())
        }
    }

    @Nested
    inner class LeaveAGroup {

        @Test
        fun `leave a group as a member`() {
            val member = UserBuilder.build()
            val group = GroupBuilder.build(members = setOf(member.userId))

            val result = group.leave(member)

            assertThat(result).isEqualTo(group.copy(members = group.members - member.userId).right())
        }

        @Test
        fun `fail leaving a group when the member was not part of it`() {
            val member = UserBuilder.build()
            val group = GroupBuilder.build()

            val result = group.leave(member)

            assertThat(result).isEqualTo(MemberWasNotPartOfTheGroup(member.userId.value).left())
        }
    }

    @Nested
    inner class IncludeAMeeting {

        @Test
        fun `include a meetup`() {
            val group = GroupBuilder.build()
            val meetup = MeetupBuilder.build()

            val result = group.include(meetup)

            assertThat(result).isEqualTo(group.copy(meetups = group.meetups + meetup.id).right())
        }

        @Test
        fun `fail including meetup when it was already included before`() {
            val meetup = MeetupBuilder.build()
            val group = GroupBuilder.build(meetups = setOf(meetup.id))

            val result = group.include(meetup)

            assertThat(result).isEqualTo(AlreadyIncluded(meetup.id.value).left())
        }
    }
}
