package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.DomainEvent
import alo.meetups.domain.model.HandleEvent
import alo.meetups.domain.model.PublishEvent

class PublishEventInMemory(private val handlers: List<HandleEvent>) : PublishEvent {

    override operator fun invoke(domainEvent: DomainEvent) = handlers.forEach { handle -> handle(domainEvent) }
}
