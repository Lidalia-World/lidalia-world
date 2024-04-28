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
import uk.org.lidalia.uri.api.Host
import uk.org.lidalia.uri.api.IpLiteral
import uk.org.lidalia.uri.api.Ipv4Address
import uk.org.lidalia.uri.api.Path
import uk.org.lidalia.uri.api.PathAndQuery
import uk.org.lidalia.uri.api.Port
import uk.org.lidalia.uri.api.Query
import uk.org.lidalia.uri.api.RegisteredName
import uk.org.lidalia.uri.api.RelativeRef
import uk.org.lidalia.uri.api.Scheme
import uk.org.lidalia.uri.api.Segment
import uk.org.lidalia.uri.api.UriReference
import uk.org.lidalia.uri.api.Url
import uk.org.lidalia.uri.api.Urn
import uk.org.lidalia.uri.api.UserInfo

private data class BasicRelativeRef(
  override val authority: Authority?,
  override val path: Path,
  override val query: Query? = null,
  override val fragment: Fragment? = null,
) : RelativeRef {

  override val scheme: Nothing? = null

  override fun resolve(toResolve: UriReference): UriReference {
    return toResolve
  }

  override fun toString(): String = "${authority?.withPrefix().orEmpty()}$path"
    .append(query)
    .append(fragment)
}

private fun Authority.withPrefix() = "//$this"

private fun CharSequence?.orEmpty() = this ?: ""

private data class BasicPathAndQuery(
  override val path: Path,
  override val query: Query? = null,
) : PathAndQuery {

  override val scheme: Nothing? = null
  override val authority: Nothing? = null

  override fun toString(): String = path.toString().append(query)

  override fun resolve(toResolve: UriReference): UriReference {
    return toResolve
  }
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

private data class BasicAbsoluteUrl(
  override val scheme: Scheme,
  override val authority: Authority,
  override val path: Path,
  override val query: Query?,
) : AbsoluteUrl {
  override val fragment: Nothing? = null

  override fun toString(): String = "$scheme://$authority$path".append(query)

  override fun resolve(toResolve: UriReference): UriReference {
    return toResolve
  }
}

private fun String.append(query: Query?): String = if (query == null) this else "$this?$query"

private fun String.append(fragment: Fragment?): String =
  if (fragment == null) this else "$this#$fragment"

private data class BasicUrl(
  override val scheme: Scheme,
  override val authority: Authority,
  override val path: Path,
  override val query: Query?,
  override val fragment: Fragment?,
) : Url {

  override fun toString(): String = "$scheme://$authority$path".append(query).append(fragment)

  override fun resolve(toResolve: UriReference): UriReference {
    return toResolve
  }
}

private data class BasicUrn(
  override val scheme: Scheme,
  override val path: Path,
  override val query: Query?,
  override val fragment: Fragment?,
) : Urn {
  override val authority: Nothing? = null

  override fun toString(): String = "$scheme:$path".append(query).append(fragment)

  override fun resolve(toResolve: UriReference): UriReference {
    return toResolve
  }
}

private data class BasicAbsoluteUrn(
  override val scheme: Scheme,
  override val path: Path,
  override val query: Query?,
) : AbsoluteUrn {
  override val authority: Nothing? = null
  override val fragment: Nothing? = null

  override fun toString(): String = "$scheme:$path".append(query)

  override fun resolve(toResolve: UriReference): UriReference {
    return toResolve
  }
}

internal fun <A : Any, B : A> CharSequenceParser<Exception, A>.castOrFail(
  input: CharSequence,
  f: (A) -> B?,
): Either<Exception, B> = this(input)
  .flatMap {
    f(it)?.right() ?: Exception("$it is of unexpected type ${it::class}").left()
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

private data class BasicPath(
  override val segments: List<Segment>,
) : Path {
  init {
    require(segments.isNotEmpty())
  }

  override fun toString(): String = segments.joinToString("/")
}

private fun MatchGroup.toScheme() = BasicScheme(value)

private fun MatchGroup.toQuery() = BasicQuery(value)

private fun MatchGroup.toFragment() = BasicFragment(value)

private fun MatchResult.extractHierarchicalPart(): Pair<Authority?, Path> {
  val authority = extractAuthorityOrNull()
  val pathStr = groups["path"]!!.value
  val segments = pathStr.split('/')
    .map(String::toSegment)
  val path = BasicPath(segments)
  return authority to path
}

@JvmInline
internal value class BasicSegment(private val value: String) : Segment, CharSequence by value {
  override fun toString(): String = value
}

internal val emptySegment: Segment = BasicSegment("")

internal val emptyPath: Path = BasicPath(listOf(emptySegment))

internal val rootPath: Path = BasicPath(listOf(emptySegment, emptySegment))

private fun String.toSegment() = BasicSegment(this)

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

private fun MatchResult.extractAuthorityOrNull(): Authority? = if (groups["authority"] == null) {
  null
} else {
  extractAuthority()
}

private fun MatchResult.extractAuthority(): BasicAuthority {
  val userInfo = groups["userInfo"]?.toUserInfo()
  val host = extractHost()
  val port = groups["port"]?.toPort()
  return BasicAuthority(userInfo, host, port)
}

private fun MatchGroup.toIpLiteral() = BasicIpLiteral(value)

private fun MatchGroup.toIpv4Address() = BasicIpv4Address(value)

private fun MatchGroup.toRegisteredName() = BasicRegisteredName(value)

private val pathRegex = """[^#?]*""".toRegex()

internal fun parsePath(input: CharSequence): Either<Exception, Path> {
  val segments = input.split('/')
    .map(String::toSegment)
  return BasicPath(segments).right()
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
    val (authority, path) = result.extractHierarchicalPart()
    if (scheme != null) {
      if (authority != null) {
        if (fragment == null) {
          BasicAbsoluteUrl(scheme, authority, path, query)
        } else {
          BasicUrl(scheme, authority, path, query, fragment)
        }
      } else {
        if (fragment == null) {
          BasicAbsoluteUrn(scheme, path, query)
        } else {
          BasicUrn(scheme, path, query, fragment)
        }
      }
    } else {
      if (fragment == null && (path.isAbsolute)) {
        BasicPathAndQuery(path, query)
      } else {
        BasicRelativeRef(authority, path, query, fragment)
      }
    }.right()
  }
}

internal fun parseScheme(input: CharSequence): Either<Exception, Scheme> =
  if (schemeRegex.matches(input)) {
    BasicScheme(input.toString()).right()
  } else {
    Exception("[$input] is not a valid scheme").left()
  }

internal fun parseAuthority(input: CharSequence): Either<Exception, Authority> {
  val result = authorityRegex.find(input)
  return result?.extractAuthority()?.right()
    ?: Exception("[$input] is not a valid authority").left()
}

internal fun parseQuery(input: CharSequence): Either<Exception, Query> =
  if (queryRegex.matches(input)) {
    BasicQuery(input.toString()).right()
  } else {
    Exception("[$input] is not a valid query").left()
  }

internal fun parseFragment(input: CharSequence): Either<Exception, Fragment> =
  if (fragmentRegex.matches(input)) {
    BasicFragment(input.toString()).right()
  } else {
    Exception("[$input] is not a valid fragment").left()
  }
