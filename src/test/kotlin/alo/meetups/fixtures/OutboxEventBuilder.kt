package alo.meetups.fixtures

import alo.meetups.infrastructure.outbox.OutboxEvent
import com.github.javafaker.Faker
import java.util.UUID

private val faker = Faker()

object OutboxEventBuilder {
    fun build(
        key: UUID = UUID.randomUUID(),
        eventPayload: ByteArray = faker.backToTheFuture().character().toByteArray(),
        stream: String = faker.superhero().name()
    ) = OutboxEvent(aggregateId = key, payload = eventPayload, stream = stream)
}
