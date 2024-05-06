package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.parseFragment

/**
 * A Fragment consists of `*( pchar / "/" / "?" )`
 */
interface Fragment : PctEncoded {
  companion object : CharSequenceParser<Exception, Fragment> {
    override operator fun invoke(input: CharSequence): Either<Exception, Fragment> =
      parseFragment(input)
  }
}

fun String.toFragment() = Fragment(this)
