package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.meetup.AttendMeetupRequest
import alo.meetups.application.services.meetup.AttendMeetupService
import alo.meetups.application.services.meetup.CancelMeetupRequest
import alo.meetups.application.services.meetup.CancelMeetupService
import alo.meetups.application.services.meetup.CreateMeetupRequest
import alo.meetups.application.services.meetup.CreateMeetupService
import alo.meetups.application.services.meetup.FinishMeetupRequest
import alo.meetups.application.services.meetup.FinishMeetupService
import alo.meetups.application.services.meetup.RateMeetupRequest
import alo.meetups.application.services.meetup.RateMeetupService
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.TooLongTopic
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Since quarkus is not providing a way to do integration test on controllers/resources in isolation (@QuarkusTest loads
 * the whole app context), unit test are performed instead.
 */
class MeetupsHttpControllerShould {

    private val faker = Faker()

    private val createMeetup = mockk<CreateMeetupService>()

    private val cancelMeetup = mockk<CancelMeetupService>()

    private val finishMeetup = mockk<FinishMeetupService>()

    private val rateMeetup = mockk<RateMeetupService>()

    private val attendMeetup = mockk<AttendMeetupService>()

    private val meetupsHttpResource = MeetupsHttpResource(
        createMeetup, cancelMeetup, finishMeetup, rateMeetup, attendMeetup
    )

    @Nested
    inner class Creating {

        @Test
        fun `should create an online meetup`() {
            val httpRequest = CreateMeetupHttpRequest.Online(
                id = UUID.randomUUID(),
                hostId = UUID.randomUUID(),
                topic = faker.chuckNorris().fact(),
                details = faker.lorem().sentence(3),
                on = ZonedDateTime.now(),
                linkName = faker.internet().domainName(),
                url = faker.internet().url()
            )
            every {
                createMeetup(
                    CreateMeetupRequest(
                        id = httpRequest.id,
                        hostId = httpRequest.hostId,
                        topic = httpRequest.topic,
                        details = httpRequest.details,
                        on = httpRequest.on,
                        type = CreateMeetupRequest.Type.Online(name = httpRequest.linkName, url = httpRequest.url)
                    )
                )
            } returns Unit.right()

            val response = meetupsHttpResource.create(httpRequest)

            assertThat(response.status).isEqualTo(201)
            assertThat(response.headers).containsEntry("Location", listOf(URI("/meetups/${httpRequest.id}")))
        }

        @Test
        fun `should create an in person meetup`() {
            val httpRequest = CreateMeetupHttpRequest.InPerson(
                id = UUID.randomUUID(),
                hostId = UUID.randomUUID(),
                topic = faker.chuckNorris().fact(),
                details = faker.lorem().sentence(3),
                on = ZonedDateTime.now(),
                address = faker.address().fullAddress(),
            )
            every {
                createMeetup(
                    CreateMeetupRequest(
                        id = httpRequest.id,
                        hostId = httpRequest.hostId,
                        topic = httpRequest.topic,
                        details = httpRequest.details,
                        on = httpRequest.on,
                        type = CreateMeetupRequest.Type.InPerson(address = httpRequest.address)
                    )
                )
            } returns Unit.right()

            val response = meetupsHttpResource.create(httpRequest)

            assertThat(response.status).isEqualTo(201)
            assertThat(response.headers).containsEntry("Location", listOf(URI("/meetups/${httpRequest.id}")))
        }

        @Test
        fun `should fail when creating a meeting fails`() {
            val httpRequest = CreateMeetupHttpRequest.InPerson(
                id = UUID.randomUUID(),
                hostId = UUID.randomUUID(),
                topic = faker.chuckNorris().fact(),
                details = faker.lorem().sentence(3),
                on = ZonedDateTime.now(),
                address = faker.address().fullAddress(),
            )
            every { createMeetup(any()) } returns TooLongTopic.left()

            val response = meetupsHttpResource.create(httpRequest)

            assertThat(response.status).isNotEqualTo(201)
        }
    }

    @Nested
    inner class Cancelling {

        @Test
        fun `should cancel a meeting`() {
            val meetupId = UUID.randomUUID()
            every { cancelMeetup(CancelMeetupRequest(meetupId, "Covid-19")) } returns Unit.right()

            val response = meetupsHttpResource.cancel(meetupId, CancelMeetupHttpRequest("Covid-19"))

            assertThat(response.status).isEqualTo(204)
        }

        @Test
        fun `should fail if cancel fails for any reason`() {
            every { cancelMeetup(any()) } returns MeetupNotFound.left()

            val response = meetupsHttpResource.cancel(UUID.randomUUID(), CancelMeetupHttpRequest("Covid-19"))

            assertThat(response.status).isNotEqualTo(204)
        }
    }

    @Nested
    inner class Attending {

        @Test
        fun `should attend to meeting`() {
            val meetupId = UUID.randomUUID()
            val attendantId = UUID.randomUUID()
            every { attendMeetup(AttendMeetupRequest(meetupId, attendantId)) } returns Unit.right()

            val response = meetupsHttpResource.attend(meetupId, AttendMeetupHttpRequest(attendantId))

            assertThat(response.status).isEqualTo(201)
        }

        @Test
        fun `should fail if attending a meeting fails for any reason`() {
            every { attendMeetup(any()) } returns MeetupNotFound.left()

            val response = meetupsHttpResource.attend(UUID.randomUUID(), AttendMeetupHttpRequest(UUID.randomUUID()))

            assertThat(response.status).isNotEqualTo(201)
        }
    }

    @Nested
    inner class Finishing {

        @Test
        fun `should finish a meeting`() {
            val meetupId = UUID.randomUUID()
            every { finishMeetup(FinishMeetupRequest(meetupId)) } returns Unit.right()

            val response = meetupsHttpResource.finish(meetupId)

            assertThat(response.status).isEqualTo(204)
        }

        @Test
        fun `should fail if finishing a meeting fails for any reason`() {
            every { finishMeetup(any()) } returns MeetupNotFound.left()

            val response = meetupsHttpResource.finish(UUID.randomUUID())

            assertThat(response.status).isNotEqualTo(204)
        }
    }

    @Nested
    inner class Rating {

        @Test
        fun `should rate a meeting`() {
            val meetupId = UUID.randomUUID()
            val attendantId= UUID.randomUUID()
            every { rateMeetup(RateMeetupRequest(meetupId, attendantId, 3)) } returns Unit.right()

            val response = meetupsHttpResource.rate(meetupId, RateMeetupHttpRequest(attendantId, 3))

            assertThat(response.status).isEqualTo(204)
        }

        @Test
        fun `should fail if rating a meeting fails for any reason`() {
            every { rateMeetup(any()) } returns MeetupNotFound.left()

            val response = meetupsHttpResource.rate(UUID.randomUUID(), RateMeetupHttpRequest(UUID.randomUUID(), 3))

            assertThat(response.status).isNotEqualTo(204)
        }
    }
}
