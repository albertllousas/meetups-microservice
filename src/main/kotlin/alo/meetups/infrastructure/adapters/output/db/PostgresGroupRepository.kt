package alo.meetups.infrastructure.adapters.output.db

import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.domain.model.group.Title
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.infrastructure.adapters.toEither
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import java.sql.ResultSet
import java.util.UUID

class PostgresGroupRepository(private val jdbi: Jdbi) : GroupRepository {

    override fun find(groupId: GroupId): Either<GroupNotFound, Group> =
        jdbi.open().use {
            it.createQuery("select * from groups where id = :id")
                .bind("id", groupId.value)
                .map { rs, _ -> rs.asGroup() }
                .findOne()
                .toEither(ifEmpty = { GroupNotFound })
        }

    private fun ResultSet.asGroup() =
        Group.reconstitute(
            groupId = GroupId(UUID.fromString(getString("id"))),
            title = Title.reconstitute(getString("title")),
            members = this.getArray("members")?.let {
                (it.array as Array<UUID>).map(::UserId).toSet()
            } ?: emptySet(),
            meetups = this.getArray("meetups")?.let {
                (it.array as Array<UUID>).map(::MeetupId).toSet()
            } ?: emptySet()
        )


    override fun create(group: Group): Either<GroupAlreadyExists, Group> = try {
        jdbi.open().use { handle ->
            handle.execute(
                """ INSERT INTO groups (id, title, members, meetups) VALUES (?,?,?,?) """,
                group.groupId.value,
                group.title.value,
                group.members.map { it.value }.toTypedArray(),
                group.meetups.map { it.value }.toTypedArray()
            )
        }.let { group.right() }
    } catch (e: UnableToExecuteStatementException) {
        if(e.message?.let {  it.contains("duplicate key") && it.contains("pk_group") } == true)
            GroupAlreadyExists.left()
        else throw e
    }

    override fun update(group: Group) {
        jdbi.open().use { handle ->
            handle.execute(
                """ UPDATE groups SET title = ?, members = ?, meetups= ? WHERE id = ? """,
                group.title.value,
                group.members.map { it.value }.toTypedArray(),
                group.meetups.map { it.value }.toTypedArray(),
                group.groupId.value
            )
        }
    }
}