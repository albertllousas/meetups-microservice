package alo.meetups.fixtures

import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.Title
import alo.meetups.domain.model.meetup.MeetupId
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

object GroupBuilder {

    fun build(
        id: GroupId = GroupId(UUID.randomUUID()),
        title: Title = Title.reconstitute(faker.book().title()),
        members: Set<UserId> = emptySet(),
        meetups: Set<MeetupId> = emptySet(),
        aggregateVersion: Long = 0
    ) = Group(id, title, members, meetups, aggregateVersion)
}