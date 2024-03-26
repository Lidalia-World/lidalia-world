package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.castOrFail
import uk.org.lidalia.uri.implementation.parseAuthority
import uk.org.lidalia.uri.implementation.parseFragment
import uk.org.lidalia.uri.implementation.parsePath
import uk.org.lidalia.uri.implementation.parseQuery
import uk.org.lidalia.uri.implementation.parseScheme
import uk.org.lidalia.uri.implementation.parseUriReference

sealed interface UriReference {
  val scheme: Scheme?
  val hierarchicalPart: HierarchicalOrRelativePart
  val authority: Authority? get() = hierarchicalPart.authority
  val path: Path get() = hierarchicalPart.path
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
  override val hierarchicalPart: HierarchicalPart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Uri> {
    override operator fun invoke(input: CharSequence): Either<Exception, Uri> =
      UriReference.castOrFail(input) { it as Uri? }
  }
}

fun String.toUri(): Either<Exception, Uri> = Uri(this)

sealed interface HierarchicalOrRelativePart {
  val authority: Authority?
  val path: Path
}

sealed interface HierarchicalPart : HierarchicalOrRelativePart {
  override val authority: Authority?
  override val path: HierarchicalPartPath
}

interface HierarchicalPartWithAuthority :
  HierarchicalPart,
  HierarchicalOrRelativePartWithAuthority {
  override val authority: Authority
  override val path: PathAbEmpty
}

interface HierarchicalPartWithoutAuthority :
  HierarchicalPart,
  HierarchicalOrRelativePartWithoutAuthority {
  override val authority: Nothing? get() = null
  override val path: HierarchicalPartPath
}

sealed interface HierarchicalOrRelativePartWithAuthority : HierarchicalOrRelativePart {
  override val authority: Authority
  override val path: PathAbEmpty
}

sealed interface HierarchicalOrRelativePartWithoutAuthority : HierarchicalOrRelativePart {
  override val authority: Nothing? get() = null
  override val path: Path
}

interface RelativeRef : UriReference {
  override val scheme: Nothing? get() = null
  override val hierarchicalPart: RelativePart
  override val path: RelativePartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, RelativeRef> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativeRef> =
      UriReference.castOrFail(input) { it as? RelativeRef }
  }
}

fun String.toRelativeRef(): Either<Exception, RelativeRef> = RelativeRef(this)

interface PathAndQuery : RelativeRef {
  override val hierarchicalPart: PathAbsolute
  override val authority: Nothing? get() = null
  override val path: PathAbsolute
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

interface UserInfo

sealed interface Host

interface IpLiteral : Host

interface Ipv4Address : Host

interface RegisteredName : Host

interface Port

sealed interface Path {
  val segments: List<Segment>

  companion object : CharSequenceParser<Exception, Path> {
    override fun invoke(input: CharSequence): Either<Exception, Path> = parsePath(input)
  }
}

fun String.toPath(): Either<Exception, Path> = Path(this)

/*
 * Empty, or an absolute path that MAY start with //
 */
interface PathAbEmpty : RelativePartPath, HierarchicalPartPath {
  override val segments: List<Segment>

  companion object : CharSequenceParser<Exception, PathAbEmpty> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathAbEmpty> =
      Path.castOrFail(input) { it as? PathAbEmpty }
  }
}

/*
 * Non-empty, starts with a /, MUST NOT start with //
 */
interface PathAbsolute :
  RelativePartPath,
  HierarchicalPartPath,
  HierarchicalPartWithoutAuthority,
  RelativePartWithoutAuthority,
  PathAbEmpty {
  override val segments: List<Segment>
  override val authority: Nothing? get() = null
  override val path: PathAbsolute get() = this

  companion object : CharSequenceParser<Exception, PathAbsolute> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathAbsolute> =
      UriReference.castOrFail(input) { it as? PathAbsolute }
  }
}

interface PathNoScheme : RelativePartPath, RelativePartWithoutAuthority, PathRootless {
  override val segments: List<Segment>

  companion object : CharSequenceParser<Exception, PathNoScheme> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathNoScheme> =
      UriReference.castOrFail(input) { it as? PathNoScheme }
  }
}

interface PathRootless : HierarchicalPartPath {
  override val segments: List<Segment>

  companion object : CharSequenceParser<Exception, PathRootless> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathRootless> =
      Path.castOrFail(input) { it as? PathRootless }
  }
}

interface PathEmpty :
  PathAbEmpty,
  PathAbsolute,
  HierarchicalPartWithoutAuthority,
  RelativePartWithoutAuthority {
  override val segments: List<Segment> get() = emptyList()
  override val authority: Nothing? get() = null
  override val path: PathEmpty get() = this

  companion object : CharSequenceParser<Exception, PathEmpty> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathEmpty> =
      UriReference.castOrFail(input) { it as? PathEmpty }
  }
}

interface Segment : CharSequence

interface Query : CharSequence {
  companion object : CharSequenceParser<Exception, Query> {
    override operator fun invoke(input: CharSequence): Either<Exception, Query> = parseQuery(input)
  }
}

fun String.toQuery() = Query(this)

interface Fragment : CharSequence {
  companion object : CharSequenceParser<Exception, Fragment> {
    override operator fun invoke(input: CharSequence): Either<Exception, Fragment> =
      parseFragment(input)
  }
}

fun String.toFragment() = Fragment(this)

sealed interface HierarchicalPartPath : Path {
  override val segments: List<Segment>

  companion object : CharSequenceParser<Exception, HierarchicalPartPath> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalPartPath> =
      Path.castOrFail(input) { it as? HierarchicalPartPath }
  }
}

sealed interface RelativePart : HierarchicalOrRelativePart, RelativeRef {
  override val scheme: Nothing? get() = null
  override val hierarchicalPart: RelativePart get() = this
  override val authority: Authority?
  override val path: RelativePartPath
  override val query: Nothing? get() = null
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, RelativePart> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePart> =
      UriReference.castOrFail(input) { it as? RelativePart }
  }
}

sealed interface RelativePartPath : Path {
  override val segments: List<Segment>

  companion object : CharSequenceParser<Exception, RelativePartPath> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePartPath> =
      Path.castOrFail(input) { it as? RelativePartPath }
  }
}

sealed interface RelativePartWithoutAuthority :
  RelativePart,
  HierarchicalOrRelativePartWithoutAuthority {
  override val authority: Nothing? get() = null

  companion object : CharSequenceParser<Exception, RelativePartWithoutAuthority> {
    override operator fun invoke(
      input: CharSequence,
    ): Either<Exception, RelativePartWithoutAuthority> =
      UriReference.castOrFail(input) { it as? RelativePartWithoutAuthority }
  }
}

interface RelativePartWithAuthority : RelativePart, HierarchicalOrRelativePartWithAuthority {
  override val authority: Authority
  override val path: PathAbEmpty
  override val hierarchicalPart: RelativePartWithAuthority get() = this

  companion object : CharSequenceParser<Exception, RelativePartWithAuthority> {
    override operator fun invoke(
      input: CharSequence,
    ): Either<Exception, RelativePartWithAuthority> = UriReference.castOrFail(input) {
      it as? RelativePartWithAuthority
    }
  }
}

interface Url : Uri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Url> {
    override operator fun invoke(input: CharSequence): Either<Exception, Url> =
      UriReference.castOrFail(input) { it as Url? }
  }
}

interface AbsoluteUrl : Url {
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrl> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrl> =
      UriReference.castOrFail(input) { it as AbsoluteUrl? }
  }
}

interface Urn : Uri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPartWithoutAuthority
  override val authority: Nothing? get() = null
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Urn> {
    override operator fun invoke(input: CharSequence): Either<Exception, Urn> =
      UriReference.castOrFail(input) { it as? Urn }
  }
}

interface AbsoluteUrn : Urn {
  override val authority: Nothing? get() = null
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrn> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrn> =
      UriReference.castOrFail(input) { it as AbsoluteUrn? }
  }
}
