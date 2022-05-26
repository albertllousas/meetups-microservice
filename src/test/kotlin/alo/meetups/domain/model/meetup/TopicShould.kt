package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.TooLongTopic
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TopicShould {

    private val faker = Faker()

    @Test
    fun `create a topic`() {
        val topic = faker.book().title()

        assertThat(Topic.create(topic)).isEqualTo(Topic.reconstitute(topic).right())
    }

    @Test
    fun `fail creating a too long topic`() {
        assertThat(Topic.create((1..351).map { it.toString() }.joinToString { "" }))
            .isEqualTo(TooLongTopic.left())
    }
}