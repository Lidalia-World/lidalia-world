package uk.org.lidalia.uri

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.lang.CharSequenceParser

interface Uri : UriReference {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Uri> {
    override operator fun invoke(input: CharSequence): Either<Exception, Uri> {
      TODO("Not yet implemented")
    }
  }
}

sealed interface HierarchicalPart : HierarchicalOrRelativePart {
  override val authority: Authority?
  override val path: HierarchicalPartPath

  companion object : CharSequenceParser<Exception, HierarchicalPart> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalPart> {
      TODO("Not yet implemented")
    }
  }
}

interface HierarchicalPartWithAuthority :
  HierarchicalPart,
  HierarchicalOrRelativePartWithAuthority {
  override val authority: Authority
  override val path: PathAbEmpty

  companion object : CharSequenceParser<Exception, HierarchicalPartWithAuthority> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalPartWithAuthority> {
      TODO("Not yet implemented")
    }
  }
}

sealed interface UriReference {
  val scheme: Scheme?
  val hierarchicalPart: HierarchicalOrRelativePart
  val authority: Authority? get() = hierarchicalPart.authority
  val path: Path get() = hierarchicalPart.path
  val query: Query?
  val fragment: Fragment?

  companion object : CharSequenceParser<Exception, UriReference> {
    override operator fun invoke(input: CharSequence): Either<Exception, UriReference> {
      TODO("Not yet implemented")
    }
  }
}

sealed interface HierarchicalOrRelativePart {
  val authority: Authority?
  val path: Path

  companion object : CharSequenceParser<Exception, HierarchicalOrRelativePart> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalOrRelativePart> {
      TODO("Not yet implemented")
    }
  }
}

sealed interface HierarchicalOrRelativePartWithAuthority : HierarchicalOrRelativePart {
  override val authority: Authority
  override val path: PathAbEmpty

  companion object : CharSequenceParser<Exception, HierarchicalOrRelativePartWithAuthority> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalOrRelativePartWithAuthority> {
      TODO("Not yet implemented")
    }
  }
}

interface AbsoluteUri : Uri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUri> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUri> {
      TODO("Not yet implemented")
    }
  }
}

interface RelativeRef : UriReference {
  override val scheme: Nothing? get() = null
  override val hierarchicalPart: RelativePart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: RelativePartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, RelativeRef> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativeRef> {
      TODO("Not yet implemented")
    }
  }
}

interface ListNonEmpty<out E> : List<E>

interface Scheme

interface Authority {
  val userInfo: UserInfo?
  val host: Host
  val port: Port?
}

interface UserInfo

sealed interface Host

interface IpLiteral : Host
interface Ipv4Address : Host
interface RegisteredName : Host

interface Port

sealed interface Path {
  val segments: List<Segment>
  val firstSegment: Segment?
  val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, Path> {
    override operator fun invoke(input: CharSequence): Either<Exception, Path> {
      TODO("Not yet implemented")
    }
  }
}

interface PathAbEmpty : RelativePartPath, HierarchicalPartPath {
  override val segments: List<Segment>
  override val firstSegment: SegmentEmpty?
  override val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, PathAbEmpty> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathAbEmpty> {
      TODO("Not yet implemented")
    }
  }
}

interface PathAbsolute : RelativePartPath, HierarchicalPartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentEmpty
  override val secondSegment: SegmentNonEmpty?

  companion object : CharSequenceParser<Exception, PathAbsolute> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathAbsolute> {
      TODO("Not yet implemented")
    }
  }
}

interface PathNoScheme : RelativePartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentNonEmptyNoColon
  override val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, PathNoScheme> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathNoScheme> {
      TODO("Not yet implemented")
    }
  }
}

interface PathRootless : HierarchicalPartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentNonEmpty
  override val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, PathRootless> {
    override operator fun invoke(input: CharSequence): Either<Exception, PathRootless> {
      TODO("Not yet implemented")
    }
  }
}

object PathEmpty : PathAbEmpty, CharSequenceParser<Exception, PathEmpty> {
  override val segments: List<Segment> = emptyList()
  override val firstSegment: Nothing? = null
  override val secondSegment: Nothing? = null

  override operator fun invoke(input: CharSequence): Either<Exception, PathEmpty> =
    if (input.isEmpty()) this.right()
    else Exception().left()

  override fun toString(): String = ""
}

interface Segment
interface SegmentNonEmpty : Segment
interface SegmentNonEmptyNoColon : SegmentNonEmpty
object SegmentEmpty : Segment

interface Query

interface Fragment

sealed interface HierarchicalPartPath : Path {
  override val segments: List<Segment>
  override val firstSegment: Segment?
  override val secondSegment: Segment?

  companion object : CharSequenceParser<Exception, HierarchicalPartPath> {
    override operator fun invoke(input: CharSequence): Either<Exception, HierarchicalPartPath> {
      TODO("Not yet implemented")
    }
  }
}

sealed interface RelativePart : HierarchicalOrRelativePart {
  override val authority: Authority?
  override val path: RelativePartPath

  companion object : CharSequenceParser<Exception, RelativePart> {
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePart> {
      TODO("Not yet implemented")
    }
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
    override operator fun invoke(input: CharSequence): Either<Exception, RelativePartWithAuthority> {
      TODO("Not yet implemented")
    }
  }
}

interface UrlReferenceWithAuthority : UriReference {
  override val scheme: Scheme?
  override val hierarchicalPart: HierarchicalOrRelativePartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, UrlReferenceWithAuthority> {
    override operator fun invoke(input: CharSequence): Either<Exception, UrlReferenceWithAuthority> {
      TODO("Not yet implemented")
    }
  }
}

interface Url : UrlReferenceWithAuthority, Uri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?

  companion object : CharSequenceParser<Exception, Url> {
    override operator fun invoke(input: CharSequence): Either<Exception, Url> {
      TODO("Not yet implemented")
    }
  }
}

interface AbsoluteUrl : Url, AbsoluteUri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Nothing? get() = null

  companion object : CharSequenceParser<Exception, AbsoluteUrl> {
    override operator fun invoke(input: CharSequence): Either<Exception, AbsoluteUrl> {
      TODO("Not yet implemented")
    }
  }
}
