package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.TooLongTopic
import arrow.core.Either
import arrow.core.left
import arrow.core.right

data class Topic private constructor(val value: String) {
    companion object {

        private const val maxLength = 350

        fun create(value: String): Either<TooLongTopic, Topic> =
            if (value.length > maxLength) TooLongTopic.left() else Topic(value).right()

        fun reconstitute(value: String): Topic = Topic(value)
    }
}