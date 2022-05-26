package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.TooLongDetails
import arrow.core.Either
import arrow.core.left
import arrow.core.right

data class Details private constructor(val value: String) {

    companion object {

        private const val maxLength = 65.536

        fun create(value: String): Either<TooLongDetails, Details> =
            if (value.length > maxLength) TooLongDetails.left() else Details(value).right()

        fun reconstitute(value: String): Details = Details(value)
    }
}
