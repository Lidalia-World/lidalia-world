package uk.org.lidalia.uri.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.collections.contain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.reflections.Reflections
import kotlin.reflect.KClass

val uriReferenceSubTypes = setOf(
  Uri::class,
  RelativeRef::class,
  Url::class,
  Urn::class,
  RelativePart::class,
  PathAndQuery::class,
  AbsoluteUrl::class,
  AbsoluteUrn::class,
  RelativePartWithAuthority::class,
  RelativePartWithoutAuthority::class,
  PathEmpty::class,
  PathAbsolute::class,
  PathNoScheme::class,
)

val hierarchicalPartSubTypes = setOf(
  HierarchicalPartWithAuthority::class,
  HierarchicalPartWithoutAuthority::class,
  PathEmpty::class,
  PathAbsolute::class,
  PathRootless::class,
)

val hierarchicalPartPathSubTypes = setOf(
  HierarchicalPartWithoutAuthority::class,
  PathAbsolute::class,
  PathAbEmpty::class,
  PathRootless::class,
  PathEmpty::class,
)

val relativePartSubTypes = setOf(
  RelativePartWithAuthority::class,
  RelativePartWithoutAuthority::class,
  PathEmpty::class,
  PathAbsolute::class,
  PathNoScheme::class,
)

val relativePartPathSubTypes = setOf(
  PathAbEmpty::class,
  PathEmpty::class,
  PathAbsolute::class,
  PathNoScheme::class,
)

val pathSubTypes = setOf(
  RelativePartPath::class,
  HierarchicalPartPath::class,
  PathAbsolute::class,
  PathAbEmpty::class,
  PathNoScheme::class,
  HierarchicalPartWithoutAuthority::class,
  PathRootless::class,
  PathEmpty::class,
)

val hierarchicalOrRelativePartWithAuthoritySubTypes = setOf(
  HierarchicalPartWithAuthority::class,
  RelativePartWithAuthority::class,
)

val hierarchicalOrRelativePartSubTypes =
  setOf(
    HierarchicalOrRelativePartWithAuthority::class,
    HierarchicalOrRelativePartWithoutAuthority::class,
    RelativePart::class,
    HierarchicalPart::class,
    HierarchicalPartWithAuthority::class,
    RelativePartWithAuthority::class,
    HierarchicalPartWithoutAuthority::class,
  ) +
    hierarchicalPartSubTypes +
    relativePartSubTypes

class ExpectedTypesSpec : StringSpec({

  val reflections = Reflections("uk.org.lidalia.uri")

  withData(
    subTypesOf<UriReference>(uriReferenceSubTypes),
    subTypesOf<HierarchicalPart>(hierarchicalPartSubTypes),
    subTypesOf<HierarchicalPartPath>(hierarchicalPartPathSubTypes),
    subTypesOf<RelativePart>(relativePartSubTypes),
    subTypesOf<RelativePartPath>(relativePartPathSubTypes),
    subTypesOf<Path>(pathSubTypes),
    subTypesOf<HierarchicalOrRelativePart>(hierarchicalOrRelativePartSubTypes),
    subTypesOf<HierarchicalOrRelativePartWithAuthority>(
      hierarchicalOrRelativePartWithAuthoritySubTypes,
    ),
  ) { (parentType, subTypes) ->
    reflections.getSubTypesOf(parentType) shouldBe subTypes
  }

  "PathAbEmpty is not a subtype of RelativePart" {
    PathAbEmpty::class.supertypes shouldNot contain(RelativePart::class)
  }
})

private inline fun <reified T : Any> subTypesOf(subTypes: Set<KClass<out T>>) = SubTypesTestCase(
  T::class,
  subTypes,
)

data class SubTypesTestCase<T : Any>(
  val kClass: KClass<T>,
  val subTypes: Set<KClass<out T>>,
) : WithDataTestName {
  override fun dataTestName() =
    "The subtypes of ${kClass.simpleName} are ${subTypes.map { it.simpleName }}"
}

private fun <T : Any> Reflections.getSubTypesOf(kClass: KClass<T>): Set<KClass<out T>> =
  getSubTypesOf(kClass.java)
    .map { it.kotlin }
    .filter { it.qualifiedName?.startsWith("uk.org.lidalia.uri.api.") == true }
    .toSet()
