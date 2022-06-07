package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.DomainEvent
import alo.meetups.domain.model.HandleEvent
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag

class HandleMetrics(private val metrics: MeterRegistry) : HandleEvent {

    override fun invoke(domainEvent: DomainEvent) {
        val tags = listOf(Tag.of("type", domainEvent::class.simpleName!!))
        metrics.counter("domain.event", tags).increment()
    }
}
