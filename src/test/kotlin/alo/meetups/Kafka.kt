package alo.meetups

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.*
import org.apache.kafka.clients.admin.NewTopic
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

class Kafka {
    val container = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
        .also { it.start() }
        .also { createTopics(it) }

    private fun createTopics(kafka: KafkaContainer) =
        listOf(
            NewTopic("meetups", 1, 1),
            NewTopic("groups", 1, 1)
        )
            .let {
                AdminClient
                    .create(mapOf(Pair(BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)))
                    .createTopics(it)
            }
}
