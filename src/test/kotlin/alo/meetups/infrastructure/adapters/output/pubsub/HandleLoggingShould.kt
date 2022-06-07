package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.GroupEvent
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.fixtures.GroupBuilder
import alo.meetups.fixtures.MeetupBuilder
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.slf4j.helpers.NOPLogger

class HandleLoggingShould {
    private val logger = spyk(NOPLogger.NOP_LOGGER)

    private val handleLogging = HandleLogging(logger)

    @Test
    fun `handle a meetup domain event writing a log`() {
        val meetupCreated = MeetupEvent.MeetupCreated(MeetupBuilder.build())

        handleLogging(meetupCreated)

        verify {
            logger.info(
                """domain-event: 'MeetupCreated', aggregate: 'meetup', id: '${meetupCreated.meetup.id.value}'"""
            )
        }
    }

    @Test
    fun `handle a group domain event writing a log`() {
        val groupCreated = GroupEvent.GroupCreated(GroupBuilder.build())

        handleLogging(groupCreated)

        verify {
            logger.info(
                """domain-event: 'GroupCreated', aggregate: 'group', id: '${groupCreated.group.id.value}'"""
            )
        }
    }
}
