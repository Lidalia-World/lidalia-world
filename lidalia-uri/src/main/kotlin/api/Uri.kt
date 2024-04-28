package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.castOrFail
import uk.org.lidalia.uri.implementation.emptyPath
import uk.org.lidalia.uri.implementation.emptySegment
import uk.org.lidalia.uri.implementation.parseAuthority
import uk.org.lidalia.uri.implementation.parseFragment
import uk.org.lidalia.uri.implementation.parsePath
import uk.org.lidalia.uri.implementation.parseQuery
import uk.org.lidalia.uri.implementation.parseScheme
import uk.org.lidalia.uri.implementation.parseUriReference
import uk.org.lidalia.uri.implementation.rootPath

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

interface Scheme : CharSequence {
  companion object : CharSequenceParser<Exception, Scheme> {
    override operator fun invoke(input: CharSequence): Either<Exception, Scheme> =
      parseScheme(input)
  }
}

fun String.toScheme(): Either<Exception, Scheme> = Scheme(this)

interface Authority {
  val userInfo: UserInfo?
  val host: Host
  val port: Port?
  companion object : CharSequenceParser<Exception, Authority> {
    override operator fun invoke(input: CharSequence): Either<Exception, Authority> =
      parseAuthority(input)
  }
}

fun String.toAuthority(): Either<Exception, Authority> = Authority(this)

interface PctEncoded : CharSequence

interface UserInfo

sealed interface Host

interface IpLiteral : Host

interface Ipv4Address : Host

interface RegisteredName : Host, PctEncoded

interface Port

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

/**
 * A Query consists of `*( pchar / "/" / "?" )`
 */
interface Query : PctEncoded {
  companion object : CharSequenceParser<Exception, Query> {
    override operator fun invoke(input: CharSequence): Either<Exception, Query> = parseQuery(input)
  }
}

fun String.toQuery() = Query(this)

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
