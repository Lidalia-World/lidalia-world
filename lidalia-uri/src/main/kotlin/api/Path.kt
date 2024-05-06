package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.emptyPath
import uk.org.lidalia.uri.implementation.emptySegment
import uk.org.lidalia.uri.implementation.parsePath
import uk.org.lidalia.uri.implementation.rootPath

/**
 * A Path consists of 1..* Segments
 */
interface Path {

  val segments: List<Segment>

  val isAbsolute get() = segments.size >= 2 && segments.first().isEmpty()

  val isEmpty get() = segments.size == 1 && segments.first().isEmpty()

  companion object : CharSequenceParser<Exception, Path> {
    override fun invoke(input: CharSequence): Either<Exception, Path> = parsePath(input)

    val empty: Path = emptyPath

    val root: Path = rootPath
  }
}

fun String.toPath(): Either<Exception, Path> = Path(this)

/**
 * A Segment consists of `*pchar`
 */
interface Segment : PctEncoded {

  companion object {
    val empty: Segment = emptySegment
  }
}
