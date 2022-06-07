package alo.meetups.infrastructure.outbox

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class MessageRelay(private val producer: KafkaProducer<String, ByteArray>) {

    fun send(batch: List<OutboxEvent>) =
        batch.map(::toKafkaRecord)
            .forEach(producer::send)
            .also { if (batch.isNotEmpty()) producer.flush() }

    private fun toKafkaRecord(outboxMessage: OutboxEvent) =
        ProducerRecord(outboxMessage.stream, outboxMessage.aggregateId.toString(), outboxMessage.payload)
}
