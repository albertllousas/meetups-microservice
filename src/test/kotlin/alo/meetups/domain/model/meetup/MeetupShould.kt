package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.AlreadyAttendingToTheMeetup
import alo.meetups.domain.model.InvalidLinkURL
import alo.meetups.domain.model.MeetupDateAlreadyPassed
import alo.meetups.domain.model.MeetupIsNotOpenForAttendants
import alo.meetups.domain.model.MeetupNotFinishedYet
import alo.meetups.domain.model.OnlyAttendantsCanRate
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeCancelled
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeFinished
import alo.meetups.domain.model.TooLongDetails
import alo.meetups.domain.model.TooLongTopic
import alo.meetups.domain.model.meetup.MeetupStatus.Cancelled
import alo.meetups.domain.model.meetup.MeetupStatus.Finished
import alo.meetups.domain.model.meetup.MeetupStatus.Upcoming
import alo.meetups.fixtures.MeetupBuilder
import alo.meetups.fixtures.UserBuilder
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime.now
import java.util.UUID

class MeetupShould {

    private val clock = Clock.fixed(Instant.parse("2018-08-19T16:45:42.00Z"), ZoneId.of("UTC"))

    private val faker = Faker()

    private val topic = faker.chuckNorris().fact()

    private val details = faker.lorem().sentence()

    private val address = faker.address().fullAddress()

    @Nested
    inner class CreateAMeeting {

        @Test
        fun `create an upcoming in person meetup`() {
            val id = UUID.randomUUID()
            val host = UserBuilder.build()
            val on = now(clock).plusDays(2)

            val meetup = Meetup.upcomingInPerson(
                id = id,
                topic = topic,
                hostedBy = host,
                details = details,
                clock = clock,
                on = on,
                address = address
            )

            assertThat(meetup).isEqualTo(
                Meetup.reconstitute(
                    id = MeetupId(id),
                    hostedBy = host.userId,
                    status = Upcoming,
                    topic = Topic.reconstitute(topic),
                    details = Details.reconstitute(details),
                    on = on,
                    groupId = null,
                    type = MeetupType.InPerson(Address(address)),
                    attendees = emptySet(),
                    aggregateVersion = 0
                ).right()
            )
        }

        @Test
        fun `create an upcoming online meetup`() {
            val id = UUID.randomUUID()
            val host = UserBuilder.build()
            val on = now(clock).plusDays(2)
            val url = "https://${faker.company().url()}"
            val linkName = faker.company().name()

            val meetup = Meetup.upcomingOnline(
                id = id,
                topic = topic,
                hostedBy = host,
                details = details,
                clock = clock,
                on = on,
                linkUrl = url,
                linkName = linkName
            )

            assertThat(meetup).isEqualTo(
                Meetup.reconstitute(
                    id = MeetupId(id),
                    hostedBy = host.userId,
                    status = Upcoming,
                    topic = Topic.reconstitute(topic),
                    details = Details.reconstitute(details),
                    on = on,
                    groupId = null,
                    type = MeetupType.Online(Link.reconstitute(linkName, url)),
                    attendees = emptySet(),
                    aggregateVersion = 0
                ).right()
            )
        }

        @Test
        fun `fail creating an online meetup when link creation fails`() {
            val result = Meetup.upcomingOnline(
                id = UUID.randomUUID(),
                topic = topic,
                hostedBy = UserBuilder.build(),
                details = details,
                clock = clock,
                on = now(clock).minusDays(2),
                linkUrl = "boom!",
                linkName = faker.company().name()
            )
            assertThat(result.isLeft()).isTrue()
            result.tapLeft { assertThat(it).isExactlyInstanceOf(InvalidLinkURL::class.java) }

        }

        @Test
        fun `fail creating a meetup when date has already passed`() {
            assertThat(
                Meetup.upcomingInPerson(
                    id = UUID.randomUUID(),
                    topic = topic,
                    hostedBy = UserBuilder.build(),
                    details = details,
                    clock = clock,
                    on = now(clock).minusDays(2),
                    address = address
                )
            ).isEqualTo(MeetupDateAlreadyPassed.left())
        }

        @Test
        fun `fail creating a meetup when topic is too long`() {
            assertThat(
                Meetup.upcomingInPerson(
                    id = UUID.randomUUID(),
                    topic = (1..1000).map { it.toString() }.joinToString { "" },
                    hostedBy = UserBuilder.build(),
                    details = details,
                    clock = clock,
                    on = now(clock).plusDays(2),
                    address = address
                )
            ).isEqualTo(TooLongTopic.left())
        }

        @Test
        fun `fail creating a meetup when details are too long`() {
            assertThat(
                Meetup.upcomingInPerson(
                    id = UUID.randomUUID(),
                    topic = topic,
                    hostedBy = UserBuilder.build(),
                    details = (1..1000000).map { it.toString() }.joinToString { "" },
                    clock = clock,
                    on = now(clock).plusDays(2),
                    address = address
                )
            ).isEqualTo(TooLongDetails.left())
        }
    }

    @Nested
    inner class AttendToAMeeting {

        @Test
        fun `add an attendant to a meetup`() {
            val meetup = MeetupBuilder.build()
            val attendee = UserBuilder.build()

            val result = meetup.attend(attendee, clock)

            assertThat(result).isEqualTo(meetup.copy(attendees = setOf(attendee.userId)).right())
        }

        @Test
        fun `fail adding an attendant to a meetup when the meeting is not open for attendants anymore`() {
            val meetup = MeetupBuilder.build(status = Cancelled(reason = "Because of COVID-19"))
            val attendee = UserBuilder.build()

            val result = meetup.attend(attendee, clock)

            assertThat(result).isEqualTo(MeetupIsNotOpenForAttendants.left())
        }

        @Test
        fun `fail adding an attendant to a meetup when the date already passed`() {
            val meetup = MeetupBuilder.build(on = now(clock).minusDays(1))
            val attendee = UserBuilder.build()

            val result = meetup.attend(attendee, clock)

            assertThat(result).isEqualTo(MeetupDateAlreadyPassed.left())
        }

        @Test
        fun `fail adding an attendant to a meetup if they were already attending`() {
            val attendee = UserBuilder.build()
            val meetup = MeetupBuilder.build(attendees = setOf(attendee.userId))

            val result = meetup.attend(attendee, clock)

            assertThat(result).isEqualTo(AlreadyAttendingToTheMeetup(attendee.userId.value).left())
        }
    }

    @Nested
    inner class RatingAMeeting {

        @Test
        fun `rate a meeting`() {
            val attendee = UserBuilder.build()
            val meetup = MeetupBuilder.build(
                status = Finished(Rating.reconstitute(stars = 3.toBigDecimal(), votes = 6)),
                attendees = setOf(attendee.userId)
            )

            val result = meetup.rate(4, attendee)

            assertThat(result)
                .isEqualTo(
                    meetup.copy(status = Finished(
                        rating = Rating.reconstitute(stars = 3.5.toBigDecimal(), votes = 7))
                    ).right()
                )
        }

        @Test
        fun `fail rating a meeting when meeting is not finished yet`() {
            val attendee = UserBuilder.build()
            val meetup = MeetupBuilder.build()

            val result = meetup.rate(4, attendee)

            assertThat(result).isEqualTo(MeetupNotFinishedYet.left())
        }

        @Test
        fun `fail rating a meeting when the user didn't attend to the meetup`() {
            val attendee = UserBuilder.build()
            val finished = MeetupBuilder.finished()

            val result = finished.rate(4, attendee)

            assertThat(result).isEqualTo(OnlyAttendantsCanRate.left())
        }
    }

    @Nested
    inner class CancelAMeeting {

        @Test
        fun `cancel a meeting`() {
            val meetup = MeetupBuilder.build()

            val result = meetup.cancel(reason = "Covid-19")

            assertThat(result).isEqualTo(meetup.copy(status = Cancelled(reason = "Covid-19")).right())
        }

        @Test
        fun `fail cancelling a non upcoming meeting`() {
            val meetup = MeetupBuilder.finished()

            val result = meetup.cancel(reason = "Covid-19")

            assertThat(result).isEqualTo(OnlyUpcomingMeetupsCanBeCancelled.left())
        }
    }

    @Nested
    inner class FinishingAMeeting {

        @Test
        fun `finish a meeting`() {
            val meetup = MeetupBuilder.build()

            val result = meetup.finish()

            assertThat(result).isEqualTo(meetup.copy(status = Finished(Rating.create())).right())
        }

        @Test
        fun `fail finishing a non upcoming meeting`() {
            val meetup = MeetupBuilder.finished()

            val result = meetup.finish()

            assertThat(result).isEqualTo(OnlyUpcomingMeetupsCanBeFinished.left())
        }

    }
}
