package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.castOrFail
import uk.org.lidalia.uri.implementation.parseUriReference

sealed interface UriReference {

  fun resolve(toResolve: UriReference): UriReference

  val scheme: Scheme?
  val authority: Authority?
  val path: Path
  val query: Query?
  val fragment: Fragment?

  companion object : CharSequenceParser<Exception, UriReference> {
    override operator fun invoke(input: CharSequence): Either<Exception, UriReference> =
      parseUriReference(input)
  }
}

fun String.toUriReference(): Either<Exception, UriReference> = UriReference(this)

sealed interface Uri : UriReference {
  override val scheme: Scheme
  override val authority: Authority?
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Uri> {
    override operator fun invoke(input: CharSequence): Either<Exception, Uri> =
      UriReference.castOrFail(input) { it as Uri? }
  }
}

fun String.toUri(): Either<Exception, Uri> = Uri(this)

interface RelativeRef : UriReference {
  override val scheme: Nothing? get() = null
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, RelativeRef> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativeRef> =
      UriReference.castOrFail(input) { it as? RelativeRef }
  }
}

fun String.toRelativeRef(): Either<Exception, RelativeRef> = RelativeRef(this)

interface PathAndQuery : RelativeRef {
  override val authority: Nothing? get() = null
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, PathAndQuery> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathAndQuery> =
      UriReference.castOrFail(input) { it as? PathAndQuery }
  }
}

fun String.toPathAndQuery(): Either<Exception, PathAndQuery> = PathAndQuery(this)

interface Url : Uri {
  override val scheme: Scheme
  override val authority: Authority
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Url> {
    override operator fun invoke(input: CharSequence): Either<Exception, Url> =
      UriReference.castOrFail(input) { it as Url? }
  }
}

interface AbsoluteUrl : Url {
  override val authority: Authority
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrl> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrl> =
      UriReference.castOrFail(input) { it as AbsoluteUrl? }
  }
}

interface Urn : Uri {
  override val scheme: Scheme
  override val authority: Nothing? get() = null
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Urn> {
    override operator fun invoke(input: CharSequence): Either<Exception, Urn> =
      UriReference.castOrFail(input) { it as? Urn }
  }
}

interface AbsoluteUrn : Urn {
  override val authority: Nothing? get() = null
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrn> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrn> =
      UriReference.castOrFail(input) { it as AbsoluteUrn? }
  }
}
