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
import uk.org.lidalia.uri.api.Path
import uk.org.lidalia.uri.api.PathAndQuery
import uk.org.lidalia.uri.api.Query
import uk.org.lidalia.uri.api.RelativeRef
import uk.org.lidalia.uri.api.Scheme
import uk.org.lidalia.uri.api.UriReference
import uk.org.lidalia.uri.api.Url
import uk.org.lidalia.uri.api.Urn

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
private value class BasicQuery(private val value: String) : Query, CharSequence by value {
  override fun toString() = value
}

@JvmInline
private value class BasicFragment(private val value: String) : Fragment, CharSequence by value {
  override fun toString() = value
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

internal val unreserved = """[a-zA-Z0-9\-._~]""".toRegex()
internal val hexDig = "[0-9A-F]".toRegex()
internal val pctEncoded = "%$hexDig{2}".toRegex()
internal val subDelims = """[!${'$'}&'()*+,;=]""".toRegex()

private fun MatchResult.extractAuthorityOrNull(): Authority? = if (groups["authority"] == null) {
  null
} else {
  extractAuthority()
}

@Language("RegExp")
internal val scheme = "(?<scheme>$schemeRegex)"

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
