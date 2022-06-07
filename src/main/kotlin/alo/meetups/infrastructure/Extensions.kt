package alo.meetups.infrastructure.adapters

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.util.Optional

@Suppress("USELESS_CAST")
fun <R, E> Optional<R>.toEither(ifEmpty: () -> E): Either<E, R> =
    this.map { it.right() as Either<E, R> }.orElseGet { ifEmpty().left() as Either<E, R> }