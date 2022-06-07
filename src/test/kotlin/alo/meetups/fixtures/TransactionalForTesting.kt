package alo.meetups.fixtures

import alo.meetups.domain.model.Transactional

class TransactionalForTesting : Transactional {
    override fun <T> invoke(transactionalBlock: () -> T): T = transactionalBlock()
}