package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.InvalidScore
import arrow.core.left
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RatingShould {

    @Test
    fun `be created with an empty state`() {
        assertThat(Rating.create()).isEqualTo(Rating.reconstitute(stars = null, votes = 0))
    }

    @Test
    fun `rate a rating with no previous votes`() {
        assertThat(Rating.create().rate(2))
            .isEqualTo(Rating.reconstitute(stars = 2.toBigDecimal(), votes = 1).right())
    }

    @Test
    fun `rate a rating with previous votes`() {
        assertThat(Rating.reconstitute(stars = 3.toBigDecimal(), votes = 3).rate(4))
            .isEqualTo(Rating.reconstitute(stars = 3.5.toBigDecimal(), votes = 4).right())
    }

    @Test
    fun `fail rating with when invalid score is provided`() {
        assertThat(Rating.create().rate(8))
            .isEqualTo(InvalidScore.left())
    }
}