package alo.meetups.domain.model.group

import alo.meetups.domain.model.TooLongTitle
import arrow.core.Either
import arrow.core.left
import arrow.core.right

data class Title private constructor(val value: String) {
    companion object {

        private const val maxLength = 250

        fun create(value: String): Either<TooLongTitle, Title> =
            if (value.length > maxLength) TooLongTitle.left() else Title(value).right()

        fun reconstitute(value: String): Title = Title(value)
    }
}