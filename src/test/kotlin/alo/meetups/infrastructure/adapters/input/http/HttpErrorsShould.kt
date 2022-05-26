package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.domain.model.AlreadyAttendingToTheMeetup
import alo.meetups.domain.model.AlreadyIncluded
import alo.meetups.domain.model.AlreadyJoined
import alo.meetups.domain.model.DomainError
import alo.meetups.domain.model.GroupAlreadyExists
import alo.meetups.domain.model.GroupNotFound
import alo.meetups.domain.model.InvalidLinkURL
import alo.meetups.domain.model.InvalidScore
import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupDateAlreadyPassed
import alo.meetups.domain.model.MeetupIsNotOpenForAttendants
import alo.meetups.domain.model.MeetupNotFinishedYet
import alo.meetups.domain.model.MeetupNotFound
import alo.meetups.domain.model.MemberWasNotPartOfTheGroup
import alo.meetups.domain.model.OnlyAttendantsCanRate
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeCancelled
import alo.meetups.domain.model.OnlyUpcomingMeetupsCanBeFinished
import alo.meetups.domain.model.TooLongDetails
import alo.meetups.domain.model.TooLongTitle
import alo.meetups.domain.model.TooLongTopic
import alo.meetups.domain.model.UserNotFound
import org.assertj.core.api.Assertions.assertThat
import org.jboss.resteasy.reactive.RestResponse.Status.BAD_REQUEST
import org.jboss.resteasy.reactive.RestResponse.Status.CONFLICT
import org.jboss.resteasy.reactive.RestResponse.notFound
import org.jboss.resteasy.reactive.RestResponse.status
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.net.MalformedURLException
import java.util.UUID.randomUUID

class HttpErrorsShould {

    @TestFactory
    fun `convert a domain error to a rest response`() =
        listOf<Pair<DomainError, Any>>(
            Pair(GroupAlreadyExists, status<Any>(CONFLICT)),
            Pair(AlreadyIncluded(randomUUID()), status<Any>(CONFLICT)),
            Pair(AlreadyJoined(randomUUID()), status<Any>(CONFLICT)),
            Pair(MemberWasNotPartOfTheGroup(randomUUID()),
                status(BAD_REQUEST, HttpError("MemberWasNotPartOfTheGroup"))),
            Pair(TooLongTitle, status(BAD_REQUEST, HttpError("TooLongTitle"))),
            Pair(GroupNotFound, notFound<Any>()),
            Pair(MeetupAlreadyExists, status<Any>(CONFLICT)),
            Pair(AlreadyAttendingToTheMeetup(randomUUID()), status<Any>(CONFLICT)),
            Pair(MeetupDateAlreadyPassed, status(BAD_REQUEST, HttpError("MeetupDateAlreadyPassed"))),
            Pair(InvalidLinkURL(MalformedURLException()), status(BAD_REQUEST, HttpError("InvalidLinkURL"))),
            Pair(MeetupIsNotOpenForAttendants,
                status(BAD_REQUEST, HttpError("MeetupIsNotOpenForAttendants"))),
            Pair(OnlyUpcomingMeetupsCanBeCancelled,
                status(BAD_REQUEST, HttpError("OnlyUpcomingMeetupsCanBeCancelled"))),
            Pair(OnlyUpcomingMeetupsCanBeFinished, status(BAD_REQUEST, HttpError("OnlyUpcomingMeetupsCanBeFinished"))),
            Pair(InvalidScore, status(BAD_REQUEST, HttpError("InvalidScore"))),
            Pair(MeetupNotFinishedYet, status(BAD_REQUEST, HttpError("MeetupNotFinishedYet"))),
            Pair(OnlyAttendantsCanRate, status(BAD_REQUEST, HttpError("OnlyAttendantsCanRate"))),
            Pair(TooLongDetails, status(BAD_REQUEST, HttpError("TooLongDetails"))),
            Pair(TooLongTopic, status(BAD_REQUEST, HttpError("TooLongTopic"))),
            Pair(MeetupNotFound, notFound<Any>()),
            Pair(UserNotFound, notFound<Any>()),
        ).map { (domainError, expectedRestResponse) ->
            dynamicTest("Convert ${domainError::class.simpleName} to a rest response") {
                val restResponse = domainError.toRestResponse<Any>()
                assertThat(restResponse).usingRecursiveComparison().isEqualTo(expectedRestResponse)
            }
        }
}
