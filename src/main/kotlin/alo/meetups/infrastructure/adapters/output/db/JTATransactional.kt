package alo.meetups.infrastructure.adapters.output.db

import alo.meetups.domain.model.Transactional
import javax.enterprise.context.ApplicationScoped
import javax.transaction.UserTransaction

@ApplicationScoped
class JTATransactional: Transactional {

    @javax.transaction.Transactional
    override fun <T> invoke(transactionalBlock: () -> T): T = transactionalBlock()
}

