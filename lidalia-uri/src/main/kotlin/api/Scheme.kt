package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.parseScheme

interface Scheme : CharSequence {
  companion object : CharSequenceParser<Exception, Scheme> {
    override operator fun invoke(input: CharSequence): Either<Exception, Scheme> =
      parseScheme(input)
  }
}

fun String.toScheme(): Either<Exception, Scheme> = Scheme(this)
