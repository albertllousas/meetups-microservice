package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.meetup.AttendMeetup
import alo.meetups.application.services.meetup.AttendMeetupRequest
import alo.meetups.application.services.meetup.CancelMeetup
import alo.meetups.application.services.meetup.CancelMeetupRequest
import alo.meetups.application.services.meetup.CreateMeetup
import alo.meetups.application.services.meetup.CreateMeetupRequest
import alo.meetups.application.services.meetup.CreateMeetupRequest.Type.InPerson
import alo.meetups.application.services.meetup.CreateMeetupRequest.Type.Online
import alo.meetups.application.services.meetup.FinishMeetup
import alo.meetups.application.services.meetup.FinishMeetupRequest
import alo.meetups.application.services.meetup.RateMeetup
import alo.meetups.application.services.meetup.RateMeetupRequest
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonMappingException
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.created
import org.jboss.resteasy.reactive.RestResponse.noContent
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import java.net.URI
import java.time.ZonedDateTime
import java.util.UUID
import javax.ws.rs.PATCH
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.Response

@Path("/meetups")
class MeetupsHttpResource(
    private val createMeetup: CreateMeetup,
    private val cancelMeetup: CancelMeetup,
    private val finishMeetup: FinishMeetup,
    private val rateMeetup: RateMeetup,
    private val attendMeetup: AttendMeetup,
) {

    @POST
    fun create(request: CreateMeetupHttpRequest): RestResponse<CreateMeetupHttpResponse> =
        createMeetup(request.toUseCaseRequest())
            .fold(
                ifLeft = { it.toRestResponse() },
                ifRight = { created(URI.create("/meetups/${request.id}")) }
            )

    private fun CreateMeetupHttpRequest.toUseCaseRequest() =
        CreateMeetupRequest(
            id = id,
            hostId = hostId,
            topic = topic,
            details = details,
            on = on,
            type = when (this) {
                is CreateMeetupHttpRequest.Online -> Online(linkName, url)
                is CreateMeetupHttpRequest.InPerson -> InPerson(address)
            }
        )

    @PATCH
    @Path("/{meetupId}/cancel")
    fun cancel(@PathParam("meetupId") meetupId: UUID, request: CancelMeetupHttpRequest): RestResponse<Unit> =
        cancelMeetup(CancelMeetupRequest(meetupId, request.reason))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { noContent() })

    @POST
    @Path("/{meetupId}/attendants")
    fun attend(@PathParam("meetupId") meetupId: UUID, request: AttendMeetupHttpRequest): RestResponse<Unit> =
        attendMeetup(AttendMeetupRequest(meetupId, request.attendantId))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { created(URI.create("/meetups/$meetupId")) })

    @PATCH
    @Path("/{meetupId}/finish")
    fun finish(@PathParam("meetupId") meetupId: UUID): RestResponse<Unit> =
        finishMeetup(FinishMeetupRequest(meetupId))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { noContent() })

    @PATCH
    @Path("/{meetupId}/rate")
    fun rate(@PathParam("meetupId") meetupId: UUID, request: RateMeetupHttpRequest): RestResponse<Unit> =
        rateMeetup(RateMeetupRequest(meetupId, request.attendantId, request.score))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { noContent() })
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = CreateMeetupHttpRequest.Online::class, name = "online"),
    JsonSubTypes.Type(value = CreateMeetupHttpRequest.InPerson::class, name = "in_person")
)
sealed class CreateMeetupHttpRequest(
    open val id: UUID,
    @get:JsonProperty("host_id")
    open val hostId: UUID,
    open val topic: String,
    open val details: String,
    open val on: ZonedDateTime,
) {
    data class Online(
        override val id: UUID,
        override val hostId: UUID,
        override val topic: String,
        override val details: String,
        override val on: ZonedDateTime,
        @get:JsonProperty("link_name")
        val linkName: String,
        val url: String,
    ) : CreateMeetupHttpRequest(id, hostId, topic, details, on)

    data class InPerson(
        override val id: UUID,
        @JsonProperty("host_id")
        override val hostId: UUID,
        override val topic: String,
        override val details: String,
        override val on: ZonedDateTime,
        val address: String,
    ) : CreateMeetupHttpRequest(id, hostId, topic, details, on)
}

data class CreateMeetupHttpResponse(val id: UUID)

data class CancelMeetupHttpRequest(val reason: String)

data class RateMeetupHttpRequest(val attendantId: UUID, val score: Int)

data class AttendMeetupHttpRequest(val attendantId: UUID)

