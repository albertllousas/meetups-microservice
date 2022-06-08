package alo.meetups.infrastructure.adapters.output.db

import java.util.UUID

data class OptimisticLockException(val aggregateId: UUID, val aggregate: String) : RuntimeException()
