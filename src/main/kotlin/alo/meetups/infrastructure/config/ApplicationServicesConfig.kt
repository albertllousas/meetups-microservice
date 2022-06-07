package alo.meetups.infrastructure.config

import alo.meetups.application.services.group.CreateGroup
import alo.meetups.application.services.group.CreateGroupService
import alo.meetups.application.services.group.IncludeMeetup
import alo.meetups.application.services.group.IncludeMeetupService
import alo.meetups.application.services.group.JoinGroup
import alo.meetups.application.services.group.JoinGroupService
import alo.meetups.application.services.group.LeaveGroup
import alo.meetups.application.services.group.LeaveGroupService
import alo.meetups.application.services.meetup.AttendMeetup
import alo.meetups.application.services.meetup.AttendMeetupService
import alo.meetups.application.services.meetup.CancelMeetup
import alo.meetups.application.services.meetup.CancelMeetupService
import alo.meetups.application.services.meetup.CreateMeetup
import alo.meetups.application.services.meetup.CreateMeetupService
import alo.meetups.application.services.meetup.FinishMeetup
import alo.meetups.application.services.meetup.FinishMeetupService
import alo.meetups.application.services.meetup.RateMeetup
import alo.meetups.application.services.meetup.RateMeetupService
import alo.meetups.domain.model.FindUser
import alo.meetups.domain.model.PublishEvent
import alo.meetups.domain.model.Transactional
import alo.meetups.domain.model.group.GroupRepository
import alo.meetups.domain.model.meetup.MeetupRepository
import alo.meetups.infrastructure.UseCaseDecorator
import java.time.Clock
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject

@ApplicationScoped
class ApplicationServicesConfig {

    @Inject
    lateinit var clock: Clock

    @Inject
    lateinit var findUser: FindUser

    @Inject
    lateinit var meetupRepository: MeetupRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var publishEvent: PublishEvent

    @Inject
    lateinit var useCaseDecorator: UseCaseDecorator

    @Inject
    lateinit var transactional: Transactional

    @Produces
    @ApplicationScoped
    fun createMeetupService(): CreateMeetup = useCaseDecorator.decorate(
        CreateMeetupService(findUser, meetupRepository, publishEvent, transactional, clock)
    )

    @Produces
    @ApplicationScoped
    fun attendMeetupService(): AttendMeetup = useCaseDecorator.decorate(
        AttendMeetupService(findUser, meetupRepository, publishEvent, transactional, clock)
    )

    @Produces
    @ApplicationScoped
    fun cancelMeetupService(): CancelMeetup = useCaseDecorator.decorate(
        CancelMeetupService(meetupRepository, publishEvent, transactional)
    )

    @Produces
    @ApplicationScoped
    fun finishMeetupService(): FinishMeetup = useCaseDecorator.decorate(
        FinishMeetupService(meetupRepository, publishEvent, transactional)
    )

    @Produces
    @ApplicationScoped
    fun rateMeetupService(): RateMeetup = useCaseDecorator.decorate(
        RateMeetupService(findUser, meetupRepository, publishEvent, transactional)
    )

    @Produces
    @ApplicationScoped
    fun createGroupService(): CreateGroup = useCaseDecorator.decorate(
        CreateGroupService(groupRepository, publishEvent, transactional)
    )

    @Produces
    @ApplicationScoped
    fun includeMeetupService(): IncludeMeetup = useCaseDecorator.decorate(
        IncludeMeetupService(groupRepository, meetupRepository, publishEvent, transactional)
    )

    @Produces
    @ApplicationScoped
    fun joinGroupService(): JoinGroup = useCaseDecorator.decorate(
        JoinGroupService(findUser, groupRepository, publishEvent, transactional)
    )

    @Produces
    @ApplicationScoped
    fun leaveGroupService(): LeaveGroup = useCaseDecorator.decorate(
        LeaveGroupService(findUser, groupRepository, publishEvent, transactional)
    )
}
