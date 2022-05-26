package alo.meetups.domain.model.meetup

import alo.meetups.domain.model.InvalidLinkURL
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.net.MalformedURLException
import java.net.URL

data class Link private constructor(val name: String, val url: URL) {

    companion object {

        fun create(name: String, url: String): Either<InvalidLinkURL, Link> =
            try {
                Link(name, URL(url)).right()
            } catch (e: MalformedURLException) {
                InvalidLinkURL(e).left()
            }

        fun reconstitute(name: String, url: String): Link = Link(name, URL(url))
    }
}
