package alo.meetups.domain.model

import java.net.MalformedURLException
import java.util.UUID

// Type of Errors

sealed interface DomainError

sealed interface CreateMeetupError : DomainError

sealed interface CancelMeetupError : DomainError

sealed interface AttendMeetupError : DomainError

sealed interface FinishMeetupError : DomainError

sealed interface RateMeetupError : DomainError

sealed interface CreateGroupError : DomainError

sealed interface IncludeMeetupError : DomainError

sealed interface JoinGroupError : DomainError

sealed interface LeaveGroupError : DomainError

// Errors

object UserNotFound : CreateMeetupError, AttendMeetupError, RateMeetupError, JoinGroupError, LeaveGroupError

// Meetup errors

object MeetupNotFound : CancelMeetupError, AttendMeetupError, FinishMeetupError, RateMeetupError, IncludeMeetupError

object MeetupAlreadyExists : CreateMeetupError

object TooLongTopic : CreateMeetupError

object TooLongDetails : CreateMeetupError

object MeetupDateAlreadyPassed : CreateMeetupError, AttendMeetupError

data class AlreadyAttendingToTheMeetup(val attendant: UUID) : AttendMeetupError

object MeetupIsNotOpenForAttendants : AttendMeetupError

object OnlyUpcomingMeetupsCanBeFinished : FinishMeetupError

object OnlyUpcomingMeetupsCanBeCancelled : CancelMeetupError

data class InvalidLinkURL(val exception: MalformedURLException) : CreateMeetupError

object InvalidScore : RateMeetupError

object MeetupNotFinishedYet : RateMeetupError

object OnlyAttendantsCanRate : RateMeetupError

// Group errors

object GroupNotFound : JoinGroupError, IncludeMeetupError, LeaveGroupError

object GroupAlreadyExists : CreateGroupError

object TooLongTitle : CreateGroupError

data class MemberWasNotPartOfTheGroup(val member: UUID) : LeaveGroupError

data class AlreadyJoined(val member: UUID) : JoinGroupError

data class AlreadyIncluded(val meetup: UUID) : IncludeMeetupError
