package lidalia.uri

interface Uri : UriReference {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?
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

sealed interface UriReference {
  val scheme: Scheme?
  val hierarchicalPart: HierarchicalOrRelativePart
  val authority: Authority? get() = hierarchicalPart.authority
  val path: Path get() = hierarchicalPart.path
  val query: Query?
  val fragment: Fragment?
}

sealed interface HierarchicalOrRelativePart {
  val authority: Authority?
  val path: Path
}

sealed interface HierarchicalOrRelativePartWithAuthority : HierarchicalOrRelativePart {
  override val authority: Authority
  override val path: PathAbEmpty
}

interface AbsoluteUri : Uri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: HierarchicalPartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Nothing? get() = null
}

interface RelativeRef : UriReference {
  override val scheme: Nothing? get() = null
  override val hierarchicalPart: RelativePart
  override val authority: Authority? get() = hierarchicalPart.authority
  override val path: RelativePartPath get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?
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
}

sealed interface RelativeAndHierarchicalPartPath : RelativePartPath, HierarchicalPartPath {
  override val segments: List<Segment>
  override val firstSegment: Segment?
  override val secondSegment: Segment?
}

interface PathAbEmpty : RelativeAndHierarchicalPartPath {
  override val firstSegment: SegmentEmpty?
  override val segments: List<Segment>
  override val secondSegment: Segment?
}

interface PathAbsolute : RelativeAndHierarchicalPartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentEmpty
  override val secondSegment: SegmentNonEmpty?
}

interface PathNoScheme : RelativePartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentNonEmptyNoColon
  override val secondSegment: Segment?
}

interface PathRootless : HierarchicalPartPath {
  override val segments: ListNonEmpty<Segment>
  override val firstSegment: SegmentNonEmpty
  override val secondSegment: Segment?
}

object PathEmpty : PathAbEmpty {
  override val segments: List<Segment> = emptyList()
  override val firstSegment: Nothing? = null
  override val secondSegment: Nothing? = null
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
}

sealed interface RelativePart : HierarchicalOrRelativePart {
  override val authority: Authority?
  override val path: RelativePartPath
}

sealed interface RelativePartPath : Path {
  override val segments: List<Segment>
  override val firstSegment: Segment?
  override val secondSegment: Segment?
}

interface RelativePartWithAuthority : RelativePart, HierarchicalOrRelativePartWithAuthority {
  override val authority: Authority
  override val path: PathAbEmpty
}

interface UrlReferenceWithAuthority : UriReference {
  override val scheme: Scheme?
  override val hierarchicalPart: HierarchicalOrRelativePartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?
}

interface Url : UrlReferenceWithAuthority, Uri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Fragment?
}

interface AbsoluteUrl : Url, AbsoluteUri {
  override val scheme: Scheme
  override val hierarchicalPart: HierarchicalPartWithAuthority
  override val authority: Authority get() = hierarchicalPart.authority
  override val path: PathAbEmpty get() = hierarchicalPart.path
  override val query: Query?
  override val fragment: Nothing? get() = null
}
