package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.InvalidLinkURL
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LinkShould {

    @Test
    fun `should create a Link`() {
        assertThat(Link.create("Some online platform", "https://some-online-platform/meeting/zxgy4"))
            .isEqualTo(Link.reconstitute("Some online platform", "https://some-online-platform/meeting/zxgy4").right())
    }

    @Test
    fun `fail creating a Link with an invalid URL`() {
        val result = Link.create("Some online platform", "invalid-url")

        assertThat(result.isLeft()).isTrue()
        result.tapLeft { assertThat(it).isExactlyInstanceOf(InvalidLinkURL::class.java) }
    }
}
