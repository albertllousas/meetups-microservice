package alo.meetups.application.services

import arrow.core.Either

interface UseCase<I, E, O> {
    operator fun invoke(request: I): Either<E, O>
}
