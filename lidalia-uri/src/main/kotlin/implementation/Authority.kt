package uk.org.lidalia.uri.implementation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.uri.api.Authority
import uk.org.lidalia.uri.api.Host
import uk.org.lidalia.uri.api.IpLiteral
import uk.org.lidalia.uri.api.Ipv4Address
import uk.org.lidalia.uri.api.Port
import uk.org.lidalia.uri.api.RegisteredName
import uk.org.lidalia.uri.api.UserInfo

private data class BasicAuthority(
  override val userInfo: UserInfo?,
  override val host: Host,
  override val port: Port?,
) : Authority {
  override fun toString(): String = userInfo.inAuthority + host + port.inAuthority
}

private val userInfoRegex = """($unreserved|$pctEncoded|$subDelims)*""".toRegex()
private val octet = "(([1-2][0-9][0-9])|([0-9][0-9])|([0-9]))".toRegex()
private val ipv4Address = "(?<ipv4Address>$octet(\\.$octet){3})".toRegex()
private val ipV6Address = """(\[(?<ipV6Address>[^]])])""".toRegex()
private val registeredName = """($unreserved|$pctEncoded|$subDelims)*""".toRegex()
private val hostRegex = "($ipV6Address|$ipv4Address|(?<registeredName>$registeredName))".toRegex()

private val portRegex = "[0-9]+".toRegex()

internal val authorityRegex =
  "((?<userInfo>$userInfoRegex)@)?(?<host>$hostRegex)(:(?<port>$portRegex))?".toRegex()

internal fun parseAuthority(input: CharSequence): Either<Exception, Authority> {
  val result = authorityRegex.find(input)
  return result?.extractAuthority()?.right()
    ?: Exception("[$input] is not a valid authority").left()
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

private val UserInfo?.inAuthority get() = if (this == null) "" else "$this@"
private val Port?.inAuthority get() = if (this == null) "" else ":$this"

internal fun MatchResult.extractAuthority(): Authority {
  val userInfo = groups["userInfo"]?.toUserInfo()
  val host = extractHost()
  val port = groups["port"]?.toPort()
  return BasicAuthority(userInfo, host, port)
}

private fun MatchResult.extractHost(): Host = groups["registeredName"]?.toRegisteredName()
  ?: groups["ipv4Address"]?.toIpv4Address()
  ?: groups["ipLiteral"]!!.toIpLiteral()

private fun MatchGroup.toIpLiteral() = BasicIpLiteral(value)

private fun MatchGroup.toIpv4Address() = BasicIpv4Address(value)

private fun MatchGroup.toRegisteredName() = BasicRegisteredName(value)

private fun MatchGroup.toUserInfo() = BasicUserInfo(value)

private fun MatchGroup.toPort() = BasicPort(value.toInt())
