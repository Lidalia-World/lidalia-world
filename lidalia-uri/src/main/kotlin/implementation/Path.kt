package uk.org.lidalia.uri.implementation

import arrow.core.Either
import arrow.core.right
import uk.org.lidalia.uri.api.Path
import uk.org.lidalia.uri.api.Segment

internal data class BasicPath(
  override val segments: List<Segment>,
) : Path {
  init {
    require(segments.isNotEmpty())
  }

  override fun toString(): String = segments.joinToString("/")
}

internal val pathRegex = """[^#?]*""".toRegex()

@JvmInline
internal value class BasicSegment(private val value: String) : Segment, CharSequence by value {
  override fun toString(): String = value
}

internal val emptySegment: Segment = BasicSegment("")

internal val emptyPath: Path = BasicPath(listOf(emptySegment))

internal val rootPath: Path = BasicPath(listOf(emptySegment, emptySegment))

internal fun String.toSegment() = BasicSegment(this)

internal fun parsePath(input: CharSequence): Either<Exception, Path> {
  val segments = input.split('/')
    .map(String::toSegment)
  return BasicPath(segments).right()
}
