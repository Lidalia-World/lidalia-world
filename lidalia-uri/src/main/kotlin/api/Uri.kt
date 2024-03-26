package uk.org.lidalia.uri.api

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.intellij.lang.annotations.Language
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.api.Authority.Companion.extractAuthority
import uk.org.lidalia.uri.api.HierarchicalPart.Companion.extractHierarchicalPart
import uk.org.lidalia.uri.api.Host.Companion.extractHost
import uk.org.lidalia.uri.api.RelativePart.Companion.extractRelativePart
import uk.org.lidalia.uri.implementation.BasicAbsoluteUrl
import uk.org.lidalia.uri.implementation.BasicAbsoluteUrn
import uk.org.lidalia.uri.implementation.BasicAuthority
import uk.org.lidalia.uri.implementation.BasicFragment
import uk.org.lidalia.uri.implementation.BasicHierarchicalPartWithAuthority
import uk.org.lidalia.uri.implementation.BasicHierarchicalPartWithoutAuthority
import uk.org.lidalia.uri.implementation.BasicIpLiteral
import uk.org.lidalia.uri.implementation.BasicIpv4Address
import uk.org.lidalia.uri.implementation.BasicPathAbEmpty
import uk.org.lidalia.uri.implementation.BasicPathAbsolute
import uk.org.lidalia.uri.implementation.BasicPathEmpty
import uk.org.lidalia.uri.implementation.BasicPathNoScheme
import uk.org.lidalia.uri.implementation.BasicPathRootless
import uk.org.lidalia.uri.implementation.BasicPort
import uk.org.lidalia.uri.implementation.BasicQuery
import uk.org.lidalia.uri.implementation.BasicRegisteredName
import uk.org.lidalia.uri.implementation.BasicRelativePartWithAuthority
import uk.org.lidalia.uri.implementation.BasicRelativeRef
import uk.org.lidalia.uri.implementation.BasicScheme
import uk.org.lidalia.uri.implementation.BasicSegmentEmpty
import uk.org.lidalia.uri.implementation.BasicSegmentNonEmpty
import uk.org.lidalia.uri.implementation.BasicSegmentNonEmptyNoColon
import uk.org.lidalia.uri.implementation.BasicUrl
import uk.org.lidalia.uri.implementation.BasicUrn
import uk.org.lidalia.uri.implementation.BasicUserInfo
import uk.org.lidalia.uri.implementation.castOrFail

sealed interface UriReference {
  val scheme: Scheme?
  val hierarchicalPart: HierarchicalOrRelativePart
  val authority: Authority? get() = hierarchicalPart.authority
  val path: Path get() = hierarchicalPart.path
  val query: Query?
  val fragment: Fragment?

  companion object : CharSequenceParser<Exception, UriReference> {
    @Language("RegExp")
    private val scheme = "(?<scheme>${Scheme.regex})"

    @Language("RegExp")
    private val authority = "(?<authority>${Authority.regex})"

    @Language("RegExp")
    private val path = "(?<path>${Path.regex})"

    @Language("RegExp")
    private val query = "(?<query>${Query.regex})"

    @Language("RegExp")
    val fragment = "(?<fragment>${Fragment.regex})"
    private val regex = """^($scheme:)?(//$authority)?$path(\?$query)?(#$fragment)?""".toRegex()

    override operator fun invoke(input: CharSequence): Either<Exception, UriReference> {
      val result = regex.find(input)
      return if (result == null) {
        Exception().left()
      } else {
        val scheme = result.groups["scheme"]?.toScheme()
        val query = result.groups["query"]?.toQuery()
        val fragment = result.groups["fragment"]?.toFragment()
        if (scheme != null) {
          when (val hierarchicalPart = result.extractHierarchicalPart()) {
            is HierarchicalPartWithAuthority -> if (fragment == null) {
              BasicAbsoluteUrl(scheme, hierarchicalPart, query)
            } else {
              BasicUrl(scheme, hierarchicalPart, query, fragment)
            }
            is HierarchicalPartWithoutAuthority -> if (fragment == null) {
              BasicAbsoluteUrn(scheme, hierarchicalPart, query)
            } else {
              BasicUrn(scheme, hierarchicalPart, query, fragment)
            }
          }
        } else {
          val relativePart = result.extractRelativePart()
          if (query != null || fragment != null) {
            BasicRelativeRef(relativePart, query, fragment)
          } else {
            relativePart
          }
        }.right()
      }
    }
  }
}

private fun MatchGroup.toScheme() = BasicScheme(value)

private fun MatchGroup.toQuery() = BasicQuery(value)

private fun MatchGroup.toFragment() = BasicFragment(value)

sealed interface Uri : UriReference {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Uri> {
    override operator fun invoke(input: CharSequence): Either<Exception, Uri> =
      UriReference(input).flatMap {
          ref ->
        (ref as? Uri)?.right() ?: Exception().left()
      }
  }
}

sealed interface HierarchicalOrRelativePart : UriReference {
  override val scheme: Nothing? get() = null
  override val hierarchicalPart: HierarchicalOrRelativePart get() = this
  override val query: Nothing? get() = null
  override val fragment: Nothing? get() = null
  override val authority: Authority?
  override val path: Path
}

sealed interface HierarchicalPart : HierarchicalOrRelativePart {
  override val authority: Authority?
  override val path: HierarchicalPartPath

  companion object : CharSequenceParser<Exception, HierarchicalPart> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalPart> =
      UriReference.castOrFail(input) { it as? HierarchicalPart }

    fun MatchResult.extractHierarchicalPart(): HierarchicalPart {
      val authority = extractAuthority()
      return if (authority == null) {
        groups["path"]!!.value.toHierarchicalPartWithoutAuthority()
      } else {
        BasicHierarchicalPartWithAuthority(authority, groups["path"]!!.value.toPathAbEmpty())
      }
    }
  }
}

fun String.toPathAbEmpty(): PathAbEmpty = if (isEmpty()) {
  BasicPathEmpty
} else {
  split('/')
    .map(String::toSegment)
    .toPathAbEmpty()
}

private fun String.toSegment() = if (isEmpty()) {
  BasicSegmentEmpty
} else if (contains(":")) {
  BasicSegmentNonEmpty(this)
} else {
  BasicSegmentNonEmptyNoColon(this)
}

fun String.toHierarchicalPartWithoutAuthority(): HierarchicalPartWithoutAuthority = if (isEmpty()) {
  BasicPathEmpty
} else {
  val segments = split('/')
    .map(String::toSegment)
  if (segments.first().isEmpty()) {
    BasicPathAbsolute(segments)
  } else {
    BasicHierarchicalPartWithoutAuthority(BasicPathRootless(segments))
  }
}

fun String.toRelativePartWithoutAuthority(): RelativePartWithoutAuthority = if (isEmpty()) {
  BasicPathEmpty
} else {
  val segments = split('/')
    .map(String::toSegment)
  if (segments.first().isEmpty()) {
    BasicPathAbsolute(segments)
  } else {
    BasicPathNoScheme(segments)
  }
}

private fun List<Segment>.toPathAbEmpty() = BasicPathAbEmpty(this)

interface HierarchicalPartWithAuthority :
  HierarchicalPart,
  HierarchicalOrRelativePartWithAuthority {
  override val authority: Authority
  override val path: PathAbEmpty
}

interface HierarchicalPartWithoutAuthority :
  HierarchicalPart,
  HierarchicalOrRelativePartWithoutAuthority,
  HierarchicalPartPath {
  override val authority: Nothing? get() = null
  override val path: HierarchicalPartPath get() = this
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
      UriReference(input).flatMap {
          ref ->
        (ref as? RelativeRef)?.right() ?: Exception().left()
      }
  }
}

interface PathAndQuery : RelativeRef {
  override val hierarchicalPart: PathAbsolute
  override val authority: Nothing? get() = null
  override val path: PathAbsolute
  override val fragment: Nothing? get() = null
}

interface Scheme : CharSequence {
  companion object {
    val regex = """[a-zA-Z][a-zA-Z0-9+\-.]*""".toRegex()
  }
}

val unreserved = """[a-zA-Z0-9\-._~]""".toRegex()
val hexDig = "[0-9A-F]".toRegex()
val pctEncoded = "%$hexDig{2}".toRegex()
val subDelims = """[!${'$'}&'()*+,;=]""".toRegex()

interface Authority {
  val userInfo: UserInfo?
  val host: Host
  val port: Port?
  companion object {
    val regex =
      "((?<userInfo>${UserInfo.regex})@)?(?<host>${Host.regex})(:(?<port>${Port.regex}))?".toRegex()

    fun MatchResult.extractAuthority(): Authority? = if (groups["authority"] == null) {
      null
    } else {
      val userInfo = groups["userInfo"]?.toUserInfo()
      val host = extractHost()
      val port = groups["port"]?.toPort()
      BasicAuthority(userInfo, host, port)
    }
  }
}

private fun MatchGroup.toUserInfo() = BasicUserInfo(value)

private fun MatchGroup.toPort() = BasicPort(value.toInt())

interface UserInfo {
  companion object {
    val regex = """($unreserved|$pctEncoded|$subDelims)*""".toRegex()
  }
}

sealed interface Host {
  companion object {
    private val octet = "(([1-2][0-9][0-9])|([0-9][0-9])|([0-9]))".toRegex()
    private val ipv4Address = "(?<ipv4Address>$octet(\\.$octet){3})".toRegex()
    private val ipV6Address = """(\[(?<ipV6Address>[^]])])""".toRegex()
    private val registeredName = """($unreserved|$pctEncoded|$subDelims)*""".toRegex()
    val regex = "($ipV6Address|$ipv4Address|(?<registeredName>$registeredName))".toRegex()

    fun MatchResult.extractHost(): Host {
      return groups["registeredName"]?.toRegisteredName()
        ?: groups["ipv4Address"]?.toIpv4Address()
        ?: groups["ipLiteral"]!!.toIpLiteral()
    }
  }
}

private fun MatchGroup.toIpLiteral() = BasicIpLiteral(value)

private fun MatchGroup.toIpv4Address() = BasicIpv4Address(value)

private fun MatchGroup.toRegisteredName() = BasicRegisteredName(value)

interface IpLiteral : Host

interface Ipv4Address : Host

interface RegisteredName : Host

interface Port {
  companion object {
    val regex = "[0-9]+".toRegex()
  }
}

sealed interface Path {
  val segments: List<Segment>
  val firstSegment: Segment?
  val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, Path> {
    val regex = """[^#?]*""".toRegex()

    override fun invoke(input: CharSequence): Either<Exception, Path> {
      val segments = input.split('/')
        .map(String::toSegment)
      return when {
        input.isEmpty() -> BasicPathEmpty
        input.startsWith("//") -> BasicPathAbEmpty(segments)
        input.startsWith("/") -> BasicPathAbsolute(segments)
        !segments.first().contains(":") -> BasicPathNoScheme(segments)
        else -> BasicPathRootless(segments)
      }.right()
    }
  }
}

/*
 * Empty, or an absolute path that MAY start with //
 */
interface PathAbEmpty : RelativePartPath, HierarchicalPartPath {
  override val segments: List<Segment>
  override val firstSegment: SegmentEmpty?
  override val secondSegment: Segment?
}

/*
 * Non-empty, starts with a /, MUST NOT start with //
 */
interface PathAbsolute :
  RelativePartPath,
  HierarchicalPartPath,
  HierarchicalPartWithoutAuthority,
  RelativePartWithoutAuthority {
  override val segments: List<Segment>
  override val firstSegment: SegmentEmpty
  override val secondSegment: SegmentNonEmpty?
  override val authority: Nothing? get() = null
  override val path: PathAbsolute get() = this
}

interface PathNoScheme : RelativePartPath, RelativePartWithoutAuthority {
  override val segments: List<Segment>
  override val firstSegment: SegmentNonEmptyNoColon
  override val secondSegment: Segment?
}

interface PathRootless : HierarchicalPartPath {
  override val segments: List<Segment>
  override val firstSegment: SegmentNonEmpty
  override val secondSegment: Segment?
}

interface PathEmpty :
  PathAbEmpty,
  HierarchicalPartWithoutAuthority,
  RelativePartWithoutAuthority {
  override val segments: List<Segment> get() = emptyList()
  override val firstSegment: Nothing? get() = null
  override val secondSegment: Nothing? get() = null
  override val authority: Nothing? get() = null
  override val path: PathEmpty get() = this
}

interface Segment : CharSequence

interface SegmentNonEmpty : Segment

interface SegmentNonEmptyNoColon : SegmentNonEmpty

interface SegmentEmpty : Segment

interface Query {
  companion object {
    val regex = "[^#]*".toRegex()
  }
}

interface Fragment {
  companion object {
    val regex = ".*".toRegex()
  }
}

sealed interface HierarchicalPartPath : Path {
  override val segments: List<Segment>
  override val firstSegment: Segment?
  override val secondSegment: Segment?
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

    fun MatchResult.extractRelativePart(): RelativePart {
      val authority = extractAuthority()
      return if (authority == null) {
        groups["path"]!!.value.toRelativePartWithoutAuthority()
      } else {
        BasicRelativePartWithAuthority(authority, groups["path"]!!.value.toPathAbEmpty())
      }
    }
  }
}

sealed interface RelativePartPath : Path, RelativePartWithoutAuthority {
  override val segments: List<Segment>
  override val firstSegment: Segment?
  override val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, RelativePartPath> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePartPath> =
      UriReference.castOrFail(input) { it as? RelativePartPath }
  }
}

interface RelativePartWithoutAuthority : RelativePart, HierarchicalOrRelativePartWithoutAuthority {
  override val authority: Nothing? get() = null
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
      UriReference(input).flatMap {
          ref ->
        (ref as? Url)?.right() ?: Exception().left()
      }
  }
}

interface AbsoluteUrl : Url {
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrl> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrl> =
      UriReference(input).flatMap {
          ref ->
        (ref as? AbsoluteUrl)?.right() ?: Exception().left()
      }
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
      UriReference(input).flatMap {
          ref ->
        (ref as? Urn)?.right() ?: Exception().left()
      }
  }
}

interface AbsoluteUrn : Urn {
  override val authority: Nothing? get() = null
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrn> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrn> =
      UriReference(input).flatMap {
          ref ->
        (ref as? AbsoluteUrn)?.right() ?: Exception().left()
      }
  }
}
