package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.domain.model.AlreadyAttendingToTheMeetup
import alo.meetups.domain.model.AlreadyIncluded
import alo.meetups.domain.model.AlreadyJoined
import alo.meetups.domain.model.DomainError
import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.UserNotFound
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.Status.BAD_REQUEST
import org.jboss.resteasy.reactive.RestResponse.Status.CONFLICT

data class HttpError(val detail: String)

@Suppress("UNCHECKED_CAST")
fun <T> DomainError.toRestResponse(): RestResponse<T> =
    when (this) {
        GroupAlreadyExists, is AlreadyIncluded, is AlreadyJoined,
        MeetupAlreadyExists, is AlreadyAttendingToTheMeetup,
        ->
            RestResponse.status(CONFLICT)
        MeetupNotFound, GroupNotFound, UserNotFound ->
            RestResponse.notFound()
        else ->
            RestResponse.status(BAD_REQUEST, HttpError(this::class.simpleName!!)) as RestResponse<T>
    }