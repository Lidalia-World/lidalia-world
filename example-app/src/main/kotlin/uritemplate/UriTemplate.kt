package uk.org.lidalia.example.uritemplate

import arrow.core.Either
import arrow.core.right
import uk.org.lidalia.uri.api.Path

interface UriTemplate {
  fun matches(path: Path): Boolean
}

internal class StringUriTemplate : UriTemplate {
  override fun matches(path: Path): Boolean = true
}

fun String.toUriTemplate(): Either<Exception, UriTemplate> = StringUriTemplate().right()

fun Path.matches(uriTemplate: UriTemplate): Boolean = uriTemplate.matches(this)
