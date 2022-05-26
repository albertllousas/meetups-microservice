package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.MeetupAlreadyExists
import alo.meetups.domain.model.MeetupNotFound
import arrow.core.Either

interface MeetupRepository {
    fun find(meetupId: MeetupId): Either<MeetupNotFound, Meetup>
    fun create(meetup: Meetup): Either<MeetupAlreadyExists, Meetup>
    fun update(meetup: Meetup)
}
