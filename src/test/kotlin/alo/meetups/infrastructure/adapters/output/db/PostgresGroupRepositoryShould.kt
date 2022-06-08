package alo.meetups.infrastructure.adapters.output.db

import alo.meetups.Postgres
import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.group.Group
import alo.meetups.domain.model.group.GroupId
import alo.meetups.domain.model.group.Title
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.fixtures.GroupBuilder
import arrow.core.left
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

@Tag("integration")
class PostgresGroupRepositoryShould {

    private val db = Postgres()

    private val jdbi = Jdbi.create(db.container.jdbcUrl, db.container.username, db.container.password)

    private val groupRepository = PostgresGroupRepository(jdbi)

    @AfterEach
    fun `tear down`() = db.container.stop()


    @Test
    fun `should find a group`() {
        val group = GroupBuilder.build(members = setOf(UserId(randomUUID()))).also(::insert)

        val result = groupRepository.find(group.id)

        assertThat(result).isEqualTo(group.right())
    }

    @Test
    fun `should fail finding a group if it does not exists`() {
        val result = groupRepository.find(GroupId(randomUUID()))

        assertThat(result).isEqualTo(GroupNotFound.left())
    }

    @Test
    fun `should create a group`() {
        val group = GroupBuilder.build(members = setOf(UserId(randomUUID())), meetups = setOf(MeetupId(randomUUID())))

        val result = groupRepository.create(group)

        assertThat(result).isEqualTo(group.right())
        assertThat(
            jdbi.open().createQuery("SELECT id FROM groups WHERE id=:id").bind(0, group.id)
        ).isNotNull
    }

    @Test
    fun `should fail creating a group if it already exists`() {
        val group = GroupBuilder.build().also(::insert)

        val result = groupRepository.create(group)

        assertThat(result).isEqualTo(GroupAlreadyExists.left())
    }

    @Test
    fun `should update a group`() {
        val group = GroupBuilder.build(
            members = setOf(UserId(randomUUID())),
            meetups = setOf(MeetupId(randomUUID()))
        ).also(::insert)
        val modifiedGroup = group.copy(
            title = Title.reconstitute("modified"),
            members = setOf(UserId(randomUUID())),
            meetups = setOf(MeetupId(randomUUID()))
        )

        groupRepository.update(modifiedGroup)

        assertThat(groupRepository.find(group.id)).isEqualTo(modifiedGroup.copy(aggregateVersion = 1).right())
    }

    @Test
    fun `should fail updating a group when there is a concurrent saving (saving an outdated version)`() {
        val group = GroupBuilder.build(
            members = setOf(UserId(randomUUID())),
            meetups = setOf(MeetupId(randomUUID())),
            aggregateVersion = 10
        ).also(::insert)
        val modifiedGroup = group.copy(
            title = Title.reconstitute("modified"),
            members = setOf(UserId(randomUUID())),
            meetups = setOf(MeetupId(randomUUID())),
            aggregateVersion = 0
        )

        assertThatThrownBy { groupRepository.update(modifiedGroup) }.isInstanceOf(OptimisticLockException::class.java)
    }

    private fun insert(group: Group) =
        jdbi.open().use { handle ->
            handle.execute(
                """ INSERT INTO groups (id, title, members, meetups, aggregate_version) VALUES (?,?,?,?,?) """,
                group.id.value,
                group.title.value,
                group.members.map { it.value }.toTypedArray(),
                group.meetups.map { it.value }.toTypedArray(),
                group.aggregateVersion
            )
        }
}
