package alo.meetups.domain.model

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
interface Transactional {
    operator fun <T> invoke(transactionalBlock: () -> T) : T
}
