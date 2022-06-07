package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.group.CreateGroup
import alo.meetups.application.services.group.CreateGroupRequest
import alo.meetups.application.services.group.IncludeMeetup
import alo.meetups.application.services.group.IncludeMeetupRequest
import alo.meetups.application.services.group.JoinGroup
import alo.meetups.application.services.group.JoinGroupRequest
import alo.meetups.application.services.group.LeaveGroup
import alo.meetups.application.services.group.LeaveGroupRequest
import com.fasterxml.jackson.annotation.JsonProperty
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.created
import org.jboss.resteasy.reactive.RestResponse.noContent
import java.net.URI
import java.util.UUID
import javax.ws.rs.DELETE
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/groups")
class GroupsHttpResource(
    private val createGroup: CreateGroup,
    private val includeMeetup: IncludeMeetup,
    private val joinGroup: JoinGroup,
    private val leaveGroup: LeaveGroup,
) {

    @POST
    fun create(request: CreateGroupHttpRequest): RestResponse<CreateMeetupHttpResponse> =
        createGroup(CreateGroupRequest(request.id, request.title))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { created(URI.create("/groups/${request.id}")) })

    @POST
    @Path("/{groupId}/meetups")
    fun include(@PathParam("groupId") groupId: UUID, request: IncludeMeetupHttpRequest): RestResponse<Unit> =
        includeMeetup(IncludeMeetupRequest(groupId, request.meetupId))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { created(URI.create("/groups/$groupId")) })

    @POST
    @Path("/{groupId}/members")
    fun join(@PathParam("groupId") groupId: UUID, request: JoinGroupHttpRequest): RestResponse<Unit> =
        joinGroup(JoinGroupRequest(groupId, request.memberId))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { created(URI.create("/groups/$groupId")) })

    @DELETE
    @Path("/{groupId}/members/{memberId}")
    fun leave(@PathParam("groupId") groupId: UUID, @PathParam("memberId") memberId: UUID): RestResponse<Unit> =
        leaveGroup(LeaveGroupRequest(groupId, memberId))
            .fold(ifLeft = { it.toRestResponse() }, ifRight = { noContent() })
}

data class CreateGroupHttpRequest(val id: UUID, val title: String)

data class IncludeMeetupHttpRequest(@get:JsonProperty("meetup_id") val meetupId: UUID)

data class JoinGroupHttpRequest(@get:JsonProperty("member_id") val memberId: UUID)
