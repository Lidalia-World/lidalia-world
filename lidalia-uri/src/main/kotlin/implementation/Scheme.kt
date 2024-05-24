package uk.org.lidalia.uri.implementation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.uri.api.Scheme

@JvmInline
internal value class BasicScheme(private val value: String) : Scheme, CharSequence by value {
  override fun toString() = value
}

internal val schemeRegex = """[a-zA-Z][a-zA-Z0-9+\-.]*""".toRegex()

internal fun MatchGroup.toScheme() = BasicScheme(value)

internal fun parseScheme(input: CharSequence): Either<Exception, Scheme> =
  if (schemeRegex.matches(input)) {
    BasicScheme(input.toString()).right()
  } else {
    Exception("[$input] is not a valid scheme").left()
  }
