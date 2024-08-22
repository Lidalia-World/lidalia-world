package uk.org.lidalia.example.uritemplate

import arrow.core.Either
import uk.org.lidalia.uri.api.Path

interface UriTemplate {
  fun matches(path: Path): Boolean
}

internal class StringUriTemplate()

fun String.toUriTemplate(): Either<Exception, UriTemplate> = TODO()

fun Path.matches(uriTemplate: UriTemplate): Boolean = uriTemplate.matches(this)
