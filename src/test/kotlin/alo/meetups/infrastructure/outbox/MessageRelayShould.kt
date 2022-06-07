package alo.meetups.infrastructure.outbox

import alo.meetups.Kafka
import alo.meetups.fixtures.OutboxEventBuilder
import alo.meetups.fixtures.buildKafkaConsumer
import alo.meetups.fixtures.buildKafkaProducer
import alo.meetups.fixtures.consumeAndAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class MessageRelayShould {

    companion object {
        val kafka = Kafka()
    }

    private val firstKafkaConsumer = buildKafkaConsumer(kafka.container.bootstrapServers, "consumer-1")
        .also { it.subscribe(listOf("topic")) }

    private val secondKafkaConsumer = buildKafkaConsumer(kafka.container.bootstrapServers, "consumer-2")
        .also { it.subscribe(listOf("topic-2")) }

    private val kafkaProducer = buildKafkaProducer(kafka.container.bootstrapServers)

    private val kafkaOutboxEventProducer = MessageRelay(kafkaProducer)

    @Test
    fun `send a batch of outbox messages successfully to different streams`() {
        val oneOutboxEvent = OutboxEventBuilder.build(stream = "topic")
        val anotherOutboxEvent = OutboxEventBuilder.build(stream = "topic-2")

        kafkaOutboxEventProducer.send(listOf(oneOutboxEvent, anotherOutboxEvent))

        firstKafkaConsumer.consumeAndAssert(stream = "topic") { record ->
            assertThat(record.key()).isEqualTo(oneOutboxEvent.aggregateId.toString())
            assertThat(record.value()).isEqualTo(oneOutboxEvent.payload)
        }
        secondKafkaConsumer.consumeAndAssert(stream = "topic-2") { record ->
            assertThat(record.key()).isEqualTo(anotherOutboxEvent.aggregateId.toString())
            assertThat(record.value()).isEqualTo(anotherOutboxEvent.payload)
        }
    }
}
