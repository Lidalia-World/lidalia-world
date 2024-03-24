package uk.org.lidalia.uri.implementation

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.api.AbsoluteUrl
import uk.org.lidalia.uri.api.AbsoluteUrn
import uk.org.lidalia.uri.api.Authority
import uk.org.lidalia.uri.api.Fragment
import uk.org.lidalia.uri.api.HierarchicalPartWithAuthority
import uk.org.lidalia.uri.api.HierarchicalPartWithoutAuthority
import uk.org.lidalia.uri.api.Host
import uk.org.lidalia.uri.api.IpLiteral
import uk.org.lidalia.uri.api.Ipv4Address
import uk.org.lidalia.uri.api.PathAbEmpty
import uk.org.lidalia.uri.api.PathAbsolute
import uk.org.lidalia.uri.api.PathEmpty
import uk.org.lidalia.uri.api.PathRootless
import uk.org.lidalia.uri.api.Port
import uk.org.lidalia.uri.api.Query
import uk.org.lidalia.uri.api.RegisteredName
import uk.org.lidalia.uri.api.RelativePart
import uk.org.lidalia.uri.api.RelativePartPath
import uk.org.lidalia.uri.api.RelativeRef
import uk.org.lidalia.uri.api.Scheme
import uk.org.lidalia.uri.api.Segment
import uk.org.lidalia.uri.api.SegmentEmpty
import uk.org.lidalia.uri.api.SegmentNonEmpty
import uk.org.lidalia.uri.api.UriReference
import uk.org.lidalia.uri.api.Url
import uk.org.lidalia.uri.api.Urn
import uk.org.lidalia.uri.api.UserInfo

internal data class BasicRelativeRef(
  override val hierarchicalPart: RelativePart,
  override val query: Query? = null,
  override val fragment: Fragment? = null,
) : RelativeRef {

  override val scheme: Nothing? = null
  override val authority: Authority? = hierarchicalPart.authority
  override val path: RelativePartPath = hierarchicalPart.path

  companion object : CharSequenceParser<Exception, RelativeRef> {
    override operator fun invoke(input: CharSequence) = castOrFail(input) { it as? RelativeRef }
  }
}

@JvmInline
internal value class BasicScheme(private val value: String) : Scheme, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicQuery(private val value: String) : Query, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicFragment(private val value: String) : Fragment, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicUserInfo(private val value: String) : UserInfo, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicIpv4Address(
  private val value: String,
) : Ipv4Address, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicIpLiteral(private val value: String) : IpLiteral, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicRegisteredName(
  private val value: String,
) : RegisteredName, CharSequence by value {
  override fun toString() = value
}

@JvmInline
internal value class BasicPort(private val value: Int) : Port {
  override fun toString() = value.toString()
}

@JvmInline
internal value class BasicSegmentNonEmpty(
  private val value: String,
) : SegmentNonEmpty, CharSequence by value {
  override fun toString() = value
}

internal object BasicPathEmpty : PathEmpty {
  override val authority: Nothing? = null
  override val path: PathEmpty = this
  override val hierarchicalPart: RelativePart = this
  override val query: Nothing? = null
  override val fragment: Nothing? = null
  override val segments: List<Segment> = emptyList()
  override val firstSegment: Nothing? = null
  override val secondSegment: Nothing? = null

  override fun toString(): String = ""
}

internal object BasicSegmentEmpty : SegmentEmpty, CharSequence {
  override val length: Int = 0

  override fun get(index: Int): Char = throw IndexOutOfBoundsException()

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
    throw IndexOutOfBoundsException()

  override fun toString(): String = ""
}

internal data class BasicPathAbEmpty(
  override val segments: List<Segment>,
) : PathAbEmpty {
  override val firstSegment: SegmentEmpty? = segments.firstOrNull() as SegmentEmpty?
  override val secondSegment: Segment? = segments.elementAtOrNull(1)

  override fun toString(): String = segments.joinToString("/")
}

internal data class BasicAbsoluteUrl(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithAuthority,
  override val query: Query?,
) : AbsoluteUrl {
  override val authority: Authority = hierarchicalPart.authority
  override val path: PathAbEmpty = hierarchicalPart.path
  override val fragment: Nothing? = null

  override fun toString(): String = "$scheme://$hierarchicalPart".append(query)

  companion object : CharSequenceParser<Exception, AbsoluteUrl> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrl> =
      castOrFail(input) { it as? AbsoluteUrl }
  }
}

private fun String.append(query: Query?): String = if (query == null) this else "$this?$query"

private fun String.append(fragment: Fragment?): String =
  if (fragment == null) this else "$this#$fragment"

internal data class BasicUrl(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithAuthority,
  override val query: Query?,
  override val fragment: Fragment?,
) : Url {
  override val authority: Authority = hierarchicalPart.authority
  override val path: PathAbEmpty = hierarchicalPart.path

  override fun toString(): String = "$scheme://$hierarchicalPart".append(query).append(fragment)

  companion object : CharSequenceParser<Exception, Url> {
    override operator fun invoke(input: CharSequence): Either<Exception, Url> =
      castOrFail(input) { it as? Url }
  }
}

internal data class BasicUrn(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithoutAuthority,
  override val query: Query?,
  override val fragment: Fragment?,
) : Urn {
  override val authority: Nothing? = null
  override val path: HierarchicalPartWithoutAuthority = hierarchicalPart.path

  companion object : CharSequenceParser<Exception, Urn> {
    override operator fun invoke(input: CharSequence): Either<Exception, Urn> =
      castOrFail(input) { it as? Urn }
  }
}

internal data class BasicAbsoluteUrn(
  override val scheme: Scheme,
  override val hierarchicalPart: HierarchicalPartWithoutAuthority,
  override val query: Query?,
) : AbsoluteUrn {
  override val authority: Nothing? = null
  override val path: HierarchicalPartWithoutAuthority = hierarchicalPart.path
  override val fragment: Nothing? = null

  override fun toString(): String = "$scheme:$hierarchicalPart".append(query)

  companion object : CharSequenceParser<Exception, AbsoluteUrn> {
    override operator fun invoke(input: CharSequence) = castOrFail(input) { it as? AbsoluteUrn }
  }
}

fun <T : UriReference> castOrFail(
  input: CharSequence,
  f: (UriReference) -> T?,
): Either<Exception, T> = UriReference(input).flatMap { f(it)?.right() ?: Exception().left() }

internal data class BasicHierarchicalPartWithAuthority(
  override val authority: Authority,
  override val path: PathAbEmpty,
) : HierarchicalPartWithAuthority {
  override fun toString(): String = "$authority$path"
}

internal data class BasicAuthority(
  override val userInfo: UserInfo?,
  override val host: Host,
  override val port: Port?,
) : Authority {
  override fun toString(): String = userInfo.inAuthority + host + port.inAuthority
}

private val UserInfo?.inAuthority get() = if (this == null) "" else "$this@"
private val Port?.inAuthority get() = if (this == null) "" else ":$this"

internal data class BasicPathAbsolute(
  override val segments: List<Segment>,
) : PathAbsolute {
  override val hierarchicalPart: PathAbsolute = this
  override val query: Nothing? = null
  override val fragment: Nothing? = null
  override val firstSegment: SegmentEmpty = segments.first() as SegmentEmpty
  override val secondSegment: SegmentNonEmpty? = segments.elementAtOrNull(1) as SegmentNonEmpty?

  override fun toString(): String = segments.joinToString("/")
}

internal data class BasicPathRootless(
  override val segments: List<Segment>,
) : PathRootless {
  override val firstSegment: SegmentNonEmpty = segments.first() as SegmentNonEmpty
  override val secondSegment: Segment? = segments.elementAtOrNull(1)

  override fun toString(): String = segments.joinToString("/")
}
