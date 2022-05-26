package alo.meetups.domain.model.group

import alo.meetups.domain.model.TooLongTitle
import arrow.core.left
import arrow.core.right
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TitleShould {

    @Test
    fun `create a title`() {
        assertThat(Title.create("Cupcakes")).isEqualTo(Title.reconstitute("Cupcakes").right())
    }

    @Test
    fun `fail creating a too long title`() {
        assertThat(Title.create((1..251).map { it.toString() }.joinToString { "" }))
            .isEqualTo(TooLongTitle.left())
    }
}