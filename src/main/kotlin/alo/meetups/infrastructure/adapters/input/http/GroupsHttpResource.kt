package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.group.CreateGroupRequest
import alo.meetups.application.services.group.CreateGroupService
import alo.meetups.application.services.group.IncludeMeetupRequest
import alo.meetups.application.services.group.IncludeMeetupService
import alo.meetups.application.services.group.JoinGroupRequest
import alo.meetups.application.services.group.JoinGroupService
import alo.meetups.application.services.group.LeaveGroupRequest
import alo.meetups.application.services.group.LeaveGroupService
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
    private val createGroup: CreateGroupService,
    private val includeMeetup: IncludeMeetupService,
    private val joinGroup: JoinGroupService,
    private val leaveGroup: LeaveGroupService,
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

data class IncludeMeetupHttpRequest(val meetupId: UUID)

data class JoinGroupHttpRequest(val memberId: UUID)
