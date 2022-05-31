package alo.meetups.infrastructure.adapters.output.db

import alo.meetups.Postgres
import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.UserId
import alo.meetups.domain.model.meetup.Meetup
import alo.meetups.domain.model.meetup.MeetupId
import alo.meetups.domain.model.meetup.MeetupStatus.Cancelled
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
import alo.meetups.domain.model.meetup.MeetupStatus.Upcoming
import alo.meetups.domain.model.meetup.MeetupType.InPerson
import alo.meetups.domain.model.meetup.MeetupType.Online
import alo.meetups.fixtures.MeetupBuilder
import arrow.core.left
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID.randomUUID

@Tag("integration")
class PostgresMeetupRepositoryShould {

    private val db = Postgres()

    private val jdbi = Jdbi.create(db.container.jdbcUrl, db.container.username, db.container.password)

    private val meetupRepository = PostgresMeetupRepository(jdbi)

    @AfterEach
    fun `tear down`() = db.container.stop()

    @Test
    fun `should find a meetup`() {
        val meetup = MeetupBuilder.build(
            on = ZonedDateTime.parse("2022-05-31T07:58:37.690Z"),
            attendees = setOf(UserId(randomUUID()))
        ).also(::insert)

        val result = meetupRepository.find(meetup.id)

        assertThat(result).isEqualTo(meetup.right())
    }

    @Test
    fun `should fail finding a meetup if it does not exists`() {
        val result = meetupRepository.find(MeetupId(randomUUID()))

        assertThat(result).isEqualTo(MeetupNotFound.left())
    }

    @Test
    fun `should create a meetup`() {
        val meetup = MeetupBuilder.build(attendees = setOf(UserId(randomUUID())))

        val result = meetupRepository.create(meetup)

        assertThat(result).isEqualTo(meetup.right())
        assertThat(
            jdbi.open().createQuery("SELECT id FROM meetups WHERE id=:id").bind(0, meetup.id)
        ).isNotNull
    }

    @Test
    fun `should fail creating a meetup if it already exists`() {
        val meetup = MeetupBuilder.build().also(::insert)

        val result = meetupRepository.create(meetup)

        assertThat(result).isEqualTo(MeetupAlreadyExists.left())
    }

    @Test
    fun `should update a meetup`() {
        val meetup = MeetupBuilder.build(attendees = setOf(UserId(randomUUID()))).also(::insert)
        val modifiedMeetup = MeetupBuilder.build(
            on = ZonedDateTime.parse("2022-05-31T07:58:37.690Z"),
            attendees = setOf(UserId(randomUUID()))
        ).copy(id = meetup.id)

        meetupRepository.update(modifiedMeetup)

        assertThat(meetupRepository.find(meetup.id)).isEqualTo(modifiedMeetup.right())
    }

    private fun insert(meetup: Meetup) =
        jdbi.open().use {
            with(meetup) {
                it.execute(
                    """ INSERT INTO meetups (
                    id, topic, details, hosted_by, on_date, group_id, attendees, meetup_type, link_name, link_url, address, 
                    status, cancel_reason, rating_stars, rating_votes
                    ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) """,
                    id.value,
                    topic.value,
                    details.value,
                    hostedBy.value,
                    on,
                    groupId?.value,
                    attendees.map { it.value }.toTypedArray(),
                    when (type) {
                        is InPerson -> "IN_PERSON"
                        is Online -> "ONLINE"
                    },
                    if (type is Online) (type as Online).link.name else null,
                    if (type is Online) (type as Online).link.url.toExternalForm() else null,
                    if (type is InPerson) (type as InPerson).address.value else null,
                    when (status) {
                        is Upcoming -> "UPCOMING"
                        is Cancelled -> "CANCELLED"
                        is Finished -> "FINISHED"
                    },
                    if (status is Cancelled) (status as Cancelled).reason else null,
                    if (status is Finished) (status as Finished).rating.stars else null,
                    if (status is Finished) (status as Finished).rating.votes else null,
                )
            }
        }
}