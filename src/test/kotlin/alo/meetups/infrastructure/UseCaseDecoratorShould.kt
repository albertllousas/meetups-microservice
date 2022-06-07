package alo.meetups.infrastructure

import alo.meetups.application.services.UseCase
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.slf4j.helpers.NOPLogger

object SomeError

class DummyUseCase : UseCase<String, SomeError, String> {
    override fun invoke(request: String): Either<SomeError, String> {
        TODO("Not yet implemented")
    }
}

class UseCaseDecoratorShould {

    private val logger = spyk(NOPLogger.NOP_LOGGER)

    private val useCaseDecorator = UseCaseDecorator(logger)

    private val useCase = mockk<DummyUseCase>()

    @Test
    fun `deal with a successful use-case execution`() {
        every { useCase("input") } returns "output".right()

        val result = useCaseDecorator.decorate(useCase)("input")

        assertThat(result).isEqualTo("output".right())
        verify { logger.info("use-case:'DummyUseCase', status: 'succeeded'") }
    }

    @Test
    fun `deal with a failed use-case execution`() {
        every { useCase("input") } returns SomeError.left()

        val result = useCaseDecorator.decorate(useCase)("input")

        assertThat(result).isEqualTo(SomeError.left())
        verify { logger.warn("use-case:'DummyUseCase', status: 'failed', domain-error: 'SomeError'") }
    }

    @Test
    fun `deal with a crashed use-case execution`() {
        val boom = Exception("Boom!")
        every { useCase("input") } throws boom

        assertThatThrownBy { useCaseDecorator.decorate(useCase)("input") }
            .isInstanceOf(Exception::class.java)
            .hasMessage("Boom!")
        verify { logger.error("use-case:'DummyUseCase', status: 'crashed'", boom) }
    }
}
