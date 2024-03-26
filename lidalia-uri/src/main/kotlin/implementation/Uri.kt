package uk.org.lidalia.uri.implementation

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.intellij.lang.annotations.Language
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.api.AbsoluteUrl
import uk.org.lidalia.uri.api.AbsoluteUrn
import uk.org.lidalia.uri.api.Authority
import uk.org.lidalia.uri.api.Fragment
import uk.org.lidalia.uri.api.HierarchicalPart
import uk.org.lidalia.uri.api.HierarchicalPartPath
import uk.org.lidalia.uri.api.HierarchicalPartWithAuthority
import uk.org.lidalia.uri.api.HierarchicalPartWithoutAuthority
import uk.org.lidalia.uri.api.Host
import uk.org.lidalia.uri.api.IpLiteral
import uk.org.lidalia.uri.api.Ipv4Address
import uk.org.lidalia.uri.api.Path
import uk.org.lidalia.uri.api.PathAbEmpty
import uk.org.lidalia.uri.api.PathAbsolute
import uk.org.lidalia.uri.api.PathEmpty
import uk.org.lidalia.uri.api.PathNoScheme
import uk.org.lidalia.uri.api.PathRootless
import uk.org.lidalia.uri.api.Port
import uk.org.lidalia.uri.api.Query
import uk.org.lidalia.uri.api.RegisteredName
import uk.org.lidalia.uri.api.RelativePart
import uk.org.lidalia.uri.api.RelativePartPath
import uk.org.lidalia.uri.api.RelativePartWithAuthority
import uk.org.lidalia.uri.api.RelativePartWithoutAuthority
import uk.org.lidalia.uri.api.RelativeRef
import uk.org.lidalia.uri.api.Scheme
import uk.org.lidalia.uri.api.Segment
import uk.org.lidalia.uri.api.UriReference
import uk.org.lidalia.uri.api.Url
import uk.org.lidalia.uri.api.Urn
import uk.org.lidalia.uri.api.UserInfo

private data class BasicRelativeRef(
  override val hierarchicalPart: RelativePart,
  override val query: Query? = null,
  override val fragment: Fragment? = null,
) : RelativeRef {

  override val scheme: Nothing? = null
  override val authority: Authority? = hierarchicalPart.authority
  override val path: RelativePartPath = hierarchicalPart.path

  override fun toString(): String = hierarchicalPart.toString().append(query).append(fragment)
}

@JvmInline
private value class BasicScheme(private val value: String) : Scheme, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicQuery(private val value: String) : Query, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicFragment(private val value: String) : Fragment, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicUserInfo(private val value: String) : UserInfo, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicIpv4Address(
  private val value: String,
) : Ipv4Address, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicIpLiteral(private val value: String) : IpLiteral, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicRegisteredName(
  private val value: String,
) : RegisteredName, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicPort(private val value: Int) : Port {
  override fun toString() = value.toString()
}

private object BasicPathEmpty : PathEmpty {
  override val authority: Nothing? = null
  override val path: PathEmpty = this
  override val hierarchicalPart: PathEmpty = this
  override val query: Nothing? = null
  override val fragment: Nothing? = null
  override val segments: List<Segment> = emptyList()

  override fun toString(): String = ""
}

private data class BasicPathAbEmpty(
  override val segments: List<Segment>,
) : PathAbEmpty {

  override fun toString(): String = segments.joinToString("/")
}

private data class BasicAbsoluteUrl(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithAuthority,
  override val query: Query?,
) : AbsoluteUrl {
  override val authority: Authority = hierarchicalPart.authority
  override val path: PathAbEmpty = hierarchicalPart.path
  override val fragment: Nothing? = null

  override fun toString(): String = "$scheme://$hierarchicalPart".append(query)
}

private fun String.append(query: Query?): String = if (query == null) this else "$this?$query"

private fun String.append(fragment: Fragment?): String =
  if (fragment == null) this else "$this#$fragment"

private data class BasicUrl(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithAuthority,
  override val query: Query?,
  override val fragment: Fragment?,
) : Url {
  override val authority: Authority = hierarchicalPart.authority
  override val path: PathAbEmpty = hierarchicalPart.path

  override fun toString(): String = "$scheme://$hierarchicalPart".append(query).append(fragment)
}

private data class BasicUrn(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithoutAuthority,
  override val query: Query?,
  override val fragment: Fragment?,
) : Urn {
  override val authority: Nothing? = null
  override val path: HierarchicalPartPath = hierarchicalPart.path

  override fun toString(): String = "$scheme:$hierarchicalPart".append(query).append(fragment)
}

private data class BasicAbsoluteUrn(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithoutAuthority,
  override val query: Query?,
) : AbsoluteUrn {
  override val authority: Nothing? = null
  override val path: HierarchicalPartPath = hierarchicalPart.path
  override val fragment: Nothing? = null

  override fun toString(): String = "$scheme:$hierarchicalPart".append(query)
}

internal fun <A : Any, B : A> CharSequenceParser<Exception, A>.castOrFail(
  input: CharSequence,
  f: (A) -> B?,
): Either<Exception, B> = this(input)
  .flatMap {
    f(it)?.right() ?: Exception("$it is of unexpected type ${it::class}").left()
  }

private data class BasicHierarchicalPartWithAuthority(
  override val authority: Authority,
  override val path: PathAbEmpty,
) : HierarchicalPartWithAuthority {
  override fun toString(): String = "$authority$path"
}

private data class BasicAuthority(
  override val userInfo: UserInfo?,
  override val host: Host,
  override val port: Port?,
) : Authority {
  override fun toString(): String = userInfo.inAuthority + host + port.inAuthority
}

private val UserInfo?.inAuthority get() = if (this == null) "" else "$this@"
private val Port?.inAuthority get() = if (this == null) "" else ":$this"

private data class BasicPathAbsolute(
  override val segments: List<Segment>,
) : PathAbsolute {
  override val hierarchicalPart: PathAbsolute = this
  override val query: Nothing? = null
  override val fragment: Nothing? = null

  override fun toString(): String = segments.joinToString("/")
}

private data class BasicPathRootless(
  override val segments: List<Segment>,
) : PathRootless {
  override fun toString(): String = segments.joinToString("/")
}

private data class BasicPathNoScheme(
  override val segments: List<Segment>,
) : PathNoScheme {
  override fun toString(): String = segments.joinToString("/")

  override val path: PathNoScheme = this
  override val hierarchicalPart: PathNoScheme = this
  override val query: Nothing? = null
  override val fragment: Nothing? = null
}

private data class BasicRelativePartWithAuthority(
  override val authority: Authority,
  override val path: PathAbEmpty,
) : RelativePartWithAuthority {
  override val query: Nothing? = null
  override val fragment: Nothing? = null

  override fun toString(): String = "//$authority$path"
}

private data class BasicHierarchicalPartWithoutAuthority(
  override val path: HierarchicalPartPath,
) : HierarchicalPartWithoutAuthority {
  override fun toString(): String = path.toString()
}

private fun MatchGroup.toScheme() = BasicScheme(value)

private fun MatchGroup.toQuery() = BasicQuery(value)

private fun MatchGroup.toFragment() = BasicFragment(value)

private fun MatchResult.extractHierarchicalPart(): HierarchicalPart {
  val authority = extractAuthority()
  return if (authority == null) {
    groups["path"]!!.value.toHierarchicalPartWithoutAuthority()
  } else {
    BasicHierarchicalPartWithAuthority(authority, groups["path"]!!.value.toPathAbEmpty())
  }
}

private fun MatchResult.extractRelativePart(): RelativePart {
  val authority = extractAuthority()
  return if (authority == null) {
    groups["path"]!!.value.toRelativePartWithoutAuthority()
  } else {
    BasicRelativePartWithAuthority(authority, groups["path"]!!.value.toPathAbEmpty())
  }
}

private fun String.toPathAbEmpty(): PathAbEmpty = if (isEmpty()) {
  BasicPathEmpty
} else {
  split('/')
    .map(String::toSegment)
    .toPathAbEmpty()
}

@JvmInline
value class BasicSegment(private val value: String) : Segment, CharSequence by value {
  override fun toString(): String = value
}

private fun String.toSegment() = BasicSegment(this)

private fun String.toHierarchicalPartWithoutAuthority(): HierarchicalPartWithoutAuthority =
  if (isEmpty()) {
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

private fun String.toRelativePartWithoutAuthority(): RelativePartWithoutAuthority = if (isEmpty()) {
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

private fun MatchGroup.toUserInfo() = BasicUserInfo(value)

private fun MatchGroup.toPort() = BasicPort(value.toInt())

private val unreserved = """[a-zA-Z0-9\-._~]""".toRegex()
private val hexDig = "[0-9A-F]".toRegex()
private val pctEncoded = "%$hexDig{2}".toRegex()
private val subDelims = """[!${'$'}&'()*+,;=]""".toRegex()
private val userInfoRegex = """($unreserved|$pctEncoded|$subDelims)*""".toRegex()
private val octet = "(([1-2][0-9][0-9])|([0-9][0-9])|([0-9]))".toRegex()
private val ipv4Address = "(?<ipv4Address>$octet(\\.$octet){3})".toRegex()
private val ipV6Address = """(\[(?<ipV6Address>[^]])])""".toRegex()
private val registeredName = """($unreserved|$pctEncoded|$subDelims)*""".toRegex()
private val hostRegex = "($ipV6Address|$ipv4Address|(?<registeredName>$registeredName))".toRegex()

private fun MatchResult.extractHost(): Host {
  return groups["registeredName"]?.toRegisteredName()
    ?: groups["ipv4Address"]?.toIpv4Address()
    ?: groups["ipLiteral"]!!.toIpLiteral()
}

private val portRegex = "[0-9]+".toRegex()
private val authorityRegex =
  "((?<userInfo>$userInfoRegex)@)?(?<host>$hostRegex)(:(?<port>$portRegex))?".toRegex()

private fun MatchResult.extractAuthority(): Authority? = if (groups["authority"] == null) {
  null
} else {
  val userInfo = groups["userInfo"]?.toUserInfo()
  val host = extractHost()
  val port = groups["port"]?.toPort()
  BasicAuthority(userInfo, host, port)
}

private fun MatchGroup.toIpLiteral() = BasicIpLiteral(value)

private fun MatchGroup.toIpv4Address() = BasicIpv4Address(value)

private fun MatchGroup.toRegisteredName() = BasicRegisteredName(value)

private val pathRegex = """[^#?]*""".toRegex()

internal fun parsePath(input: CharSequence): Either<Exception, Path> {
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

private val schemeRegex = """[a-zA-Z][a-zA-Z0-9+\-.]*""".toRegex()

@Language("RegExp")
private val scheme = "(?<scheme>$schemeRegex)"

@Language("RegExp")
private val authority = "(?<authority>$authorityRegex)"

@Language("RegExp")
private val path = "(?<path>$pathRegex)"

private val queryRegex = "[^#]*".toRegex()

@Language("RegExp")
private val query = "(?<query>$queryRegex)"

private val fragmentRegex = ".*".toRegex()

@Language("RegExp")
private val fragment = "(?<fragment>$fragmentRegex)"

private val regex = """^($scheme:)?(//$authority)?$path(\?$query)?(#$fragment)?""".toRegex()

internal fun parseUriReference(input: CharSequence): Either<Exception, UriReference> {
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
