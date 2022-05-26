package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.application.services.meetup.AttendMeetupService
import alo.meetups.application.services.meetup.CreateMeetupRequest
import alo.meetups.application.services.meetup.CreateMeetupService
import org.jboss.resteasy.reactive.RestResponse
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/meetupsasdsafdsd")
class AnotherHttpController(private val attendMeetupService: AttendMeetupService) {

    @GET
    fun get() = "hello"
}