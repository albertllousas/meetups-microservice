package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.InvalidScore
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.math.BigDecimal

data class Rating private constructor(val stars: BigDecimal?, val votes: Int) {

    companion object {

        private const val maxStars = 5

        fun create(): Rating = Rating(null, 0)

        fun reconstitute(stars: BigDecimal?, votes: Int): Rating = Rating(stars, votes)
    }

    fun rate(score: Int): Either<InvalidScore, Rating> =
        if (score in 1..maxStars)
            this.copy(
                votes = votes.inc(),
                stars = stars?.let { stars.add(score.toBigDecimal()).divide(2.toBigDecimal()) } ?: score.toBigDecimal()
            ).right()
        else InvalidScore.left()
}