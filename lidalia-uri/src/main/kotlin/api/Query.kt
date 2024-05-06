package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.parseQuery

/**
 * A Query consists of `*( pchar / "/" / "?" )`
 */
interface Query : PctEncoded {
  companion object : CharSequenceParser<Exception, Query> {
    override operator fun invoke(input: CharSequence): Either<Exception, Query> = parseQuery(input)
  }
}

fun String.toQuery() = Query(this)
