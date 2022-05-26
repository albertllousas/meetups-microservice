package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.meetup.AttendMeetupService
import alo.meetups.application.services.meetup.CancelMeetupService
import alo.meetups.application.services.meetup.CreateMeetupRequest
import alo.meetups.application.services.meetup.CreateMeetupService
import alo.meetups.application.services.meetup.FinishMeetupService
import alo.meetups.application.services.meetup.RateMeetupService
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
class MeetupHttpControllerShould {

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
}
