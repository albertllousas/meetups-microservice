package alo.meetups.infrastructure.adapters.input.http

import alo.meetups.infrastructure.adapters.output.db.OptimisticLockException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.jboss.resteasy.reactive.RestResponse

import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import javax.ws.rs.core.Response

class ExceptionMappers {

    private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    @ServerExceptionMapper(MismatchedInputException::class)
    fun mapException(exception: MismatchedInputException): RestResponse<Unit> {
        logger.warn(exception.message, exception)
        return RestResponse.status(Response.Status.BAD_REQUEST)
    }

    @ServerExceptionMapper(OptimisticLockException::class)
    fun mapException(exception: OptimisticLockException): RestResponse<Unit> {
        logger.warn("Optimistic lock exception due a concurrent saving, please try again.", exception)
        return RestResponse.status(Response.Status.BAD_REQUEST)
    }
}
