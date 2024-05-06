package uk.org.lidalia.uri.api

import arrow.core.Either
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.implementation.parseAuthority

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

interface RegisteredName : Host, PctEncoded

interface Port
