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
import uk.org.lidalia.uri.implementation.BaseHierarchicalPartWithAuthority
import uk.org.lidalia.uri.implementation.BaseHierarchicalPartWithoutAuthority
import uk.org.lidalia.uri.implementation.BasicAbsoluteUrl
import uk.org.lidalia.uri.implementation.BasicAbsoluteUrn
import uk.org.lidalia.uri.implementation.BasicAuthority
import uk.org.lidalia.uri.implementation.BasicFragment
import uk.org.lidalia.uri.implementation.BasicIpLiteral
import uk.org.lidalia.uri.implementation.BasicIpv4Address
import uk.org.lidalia.uri.implementation.BasicPathAbEmpty
import uk.org.lidalia.uri.implementation.BasicPathEmpty
import uk.org.lidalia.uri.implementation.BasicPort
import uk.org.lidalia.uri.implementation.BasicQuery
import uk.org.lidalia.uri.implementation.BasicRegisteredName
import uk.org.lidalia.uri.implementation.BasicRelativeRef
import uk.org.lidalia.uri.implementation.BasicScheme
import uk.org.lidalia.uri.implementation.BasicSegmentEmpty
import uk.org.lidalia.uri.implementation.BasicSegmentNonEmpty
import uk.org.lidalia.uri.implementation.BasicUrl
import uk.org.lidalia.uri.implementation.BasicUrn
import uk.org.lidalia.uri.implementation.BasicUserInfo

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
          BasicRelativeRef(relativePart, query, fragment)
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

sealed interface HierarchicalOrRelativePart {
  val authority: Authority?
  val path: Path
}

sealed interface HierarchicalPart : HierarchicalOrRelativePart {
  override val authority: Authority?
  override val path: HierarchicalPartPath

  companion object : CharSequenceParser<Exception, HierarchicalPart> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalPart> =
      UriReference(input).flatMap {
          ref ->
        (ref as? HierarchicalPart)?.right() ?: Exception().left()
      }

    fun MatchResult.extractHierarchicalPart(): HierarchicalPart {
      val authority = extractAuthority()
      val path = groups["path"]!!.value.toPathAbEmpty()
      return if (authority == null) {
        BaseHierarchicalPartWithoutAuthority(path)
      } else {
        BaseHierarchicalPartWithAuthority(authority, path)
      }
    }
  }
}

fun String.toPathAbEmpty(): PathAbEmpty = if (isEmpty()) {
  BasicPathEmpty
} else {
  split('/')
    .map {
      if (it.isEmpty()) {
        BasicSegmentEmpty
      } else {
        BasicSegmentNonEmpty(it)
      }
    }
    .toPathAbEmpty()
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
  HierarchicalOrRelativePartWithoutAuthority {
  override val authority: Nothing? get() = null
  override val path: PathAbEmpty
}

sealed interface HierarchicalOrRelativePartWithAuthority : HierarchicalOrRelativePart {
  override val authority: Authority
  override val path: PathAbEmpty

  companion object : CharSequenceParser<Exception, HierarchicalOrRelativePartWithAuthority> {
    override operator fun invoke(
      input: CharSequence,
    ): Either<Exception, HierarchicalOrRelativePartWithAuthority> {
      TODO("Not yet implemented")
    }
  }
}

sealed interface HierarchicalOrRelativePartWithoutAuthority : HierarchicalOrRelativePart {
  override val authority: Nothing? get() = null
  override val path: PathAbEmpty
}

interface RelativeRef : UriReference {
  override val scheme: Nothing? get() = null
  override val hierarchicalPart: RelativePart
  override val path: RelativePartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?
}

interface ListNonEmpty<out E> : List<E>

interface Scheme : CharSequence {
  companion object {
    val regex = """[a-zA-Z][a-zA-Z0-9+\-.]+""".toRegex()
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

    override fun invoke(p1: CharSequence): Either<Exception, Path> {
      TODO("Not yet implemented")
    }
  }
}

interface PathAbEmpty : RelativePartPath, HierarchicalPartPath {
  override val segments: List<Segment>
  override val firstSegment: SegmentEmpty?
  override val secondSegment: Segment?
}

interface PathAbsolute : RelativePartPath, HierarchicalPartPath, RelativePart {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentEmpty
  override val secondSegment: SegmentNonEmpty?
}

interface PathNoScheme : RelativePartPath, RelativePart {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentNonEmptyNoColon
  override val secondSegment: Segment?
}

interface PathRootless : HierarchicalPartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentNonEmpty
  override val secondSegment: Segment?
}

interface PathEmpty : PathAbEmpty, RelativePart {
  override val segments: List<Segment> get() = emptyList()
  override val firstSegment: Nothing? get() = null
  override val secondSegment: Nothing? get() = null
}

interface Segment

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

interface RelativePart : HierarchicalOrRelativePart, RelativeRef {
  override val authority: Authority?
  override val path: RelativePartPath
  companion object : CharSequenceParser<Exception, RelativePart> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePart> {
      TODO("Not yet implemented")
    }

    fun MatchResult.extractRelativePart(): RelativePart = TODO()
  }
}

sealed interface RelativePartPath : Path {
  override val segments: List<Segment>
  override val firstSegment: Segment?
  override val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, RelativePartPath> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePartPath> {
      TODO("Not yet implemented")
    }
  }
}

interface RelativePartWithAuthority : RelativePart, HierarchicalOrRelativePartWithAuthority {
  override val authority: Authority
  override val path: PathAbEmpty

  companion object : CharSequenceParser<Exception, RelativePartWithAuthority> {
    override operator fun invoke(
      input: CharSequence,
    ): Either<Exception, RelativePartWithAuthority> {
      TODO("Not yet implemented")
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
  override val path: PathAbEmpty get() = hierarchicalPart.path
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
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrn> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrn> =
      UriReference(input).flatMap {
          ref ->
        (ref as? AbsoluteUrn)?.right() ?: Exception().left()
      }
  }
}
