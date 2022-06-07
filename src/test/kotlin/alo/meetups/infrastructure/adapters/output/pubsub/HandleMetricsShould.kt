package alo.meetups.infrastructure.adapters.output.pubsub

import alo.meetups.domain.model.MeetupEvent
import alo.meetups.fixtures.MeetupBuilder
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class HandleMetricsShould {

    private val metrics = SimpleMeterRegistry()

    private val handleMetrics = HandleMetrics(metrics)

    @Test
    fun `handle a domain event publishing a metric`() {
        val meetupCreated = MeetupEvent.MeetupCreated(MeetupBuilder.build())

        handleMetrics(meetupCreated)

        assertThat(metrics.counter("domain.event", listOf(Tag.of("type", "MeetupCreated"))).count())
            .isEqualTo(1.0)
    }
}