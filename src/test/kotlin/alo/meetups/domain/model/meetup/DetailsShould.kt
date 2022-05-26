package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.TooLongDetails
import arrow.core.left
import arrow.core.right
import com.github.javafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DetailsShould {

    private val faker = Faker()

    @Test
    fun `create details`() {
        val details = faker.lorem().sentence()

        assertThat(Details.create(details)).isEqualTo(Details.reconstitute(details).right())
    }

    @Test
    fun `fail creating a too long details`() {
        assertThat(Details.create((1..66000).map { it.toString() }.joinToString { "" }))
            .isEqualTo(TooLongDetails.left())
    }
}