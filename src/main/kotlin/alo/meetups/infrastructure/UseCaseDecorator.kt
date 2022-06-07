package alo.meetups.infrastructure

import alo.meetups.application.services.UseCase
import arrow.core.Either
import arrow.core.Either.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

class UseCaseDecorator(
    val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()),
) {
    fun <I, E, O> decorate( useCase: UseCase<I, E, O>): UseCase<I,E, O> = object: UseCase<I,E, O>{
        override fun invoke(request: I): Either<E,O> = try {
            val result = useCase.invoke(request)
            when(result) {
                is Left ->
                    logger.warn("use-case:'${useCase.javaClass.simpleName}', status: 'failed', domain-error: '${result.value!!.asString()}'")
                is Right ->
                    logger.info("use-case:'${useCase.javaClass.simpleName}', status: 'succeeded'")
            }
             result
        } catch(exception: Exception) {
            logger.error("use-case:'${useCase.javaClass.simpleName}', status: 'crashed'", exception)
            throw exception
        }
    }

    private fun Any.asString(): String =
        if(this::class.objectInstance != null) this::class.simpleName!!.split('.').last()
        else this.toString()
}
