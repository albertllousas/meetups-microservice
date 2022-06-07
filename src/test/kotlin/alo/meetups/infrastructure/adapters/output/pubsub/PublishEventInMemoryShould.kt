package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.HandleEvent
import alo.meetups.domain.model.MeetupEvent
import alo.meetups.fixtures.MeetupBuilder
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PublishEventInMemoryShould {

    @Test
    fun `notify domain event to the handlers`() {
        val event = MeetupEvent.MeetupCreated(MeetupBuilder.build())
        val firstHandler = mockk<HandleEvent>(relaxed = true)
        val secondHandler = mockk<HandleEvent>(relaxed = true)
        val thirdHandler = mockk<HandleEvent>(relaxed = true)
        val publish = PublishEventInMemory(
            listOf(firstHandler, secondHandler, thirdHandler)
        )

        publish(event)

        verify {
            firstHandler.invoke(event)
            secondHandler.invoke(event)
            thirdHandler.invoke(event)
        }
    }
}