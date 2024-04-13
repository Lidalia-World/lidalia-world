package uk.org.lidalia.uri.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.reflections.Reflections
import kotlin.reflect.KClass

val uriSubTypes: Set<KClass<out Uri>> = setOf(
  Url::class,
  Urn::class,
  AbsoluteUrl::class,
  AbsoluteUrn::class,
)

val uriReferenceSubTypes: Set<KClass<out UriReference>> = setOf(
  Uri::class,
  RelativeRef::class,
  PathAndQuery::class,
) + uriSubTypes

class ExpectedTypesSpec : StringSpec({

  val reflections = Reflections("uk.org.lidalia.uri")

  withData(
    subTypesOf<Uri>(uriSubTypes),
    subTypesOf<UriReference>(uriReferenceSubTypes),
    subTypesOf<RelativeRef>(setOf(PathAndQuery::class)),
  ) { (parentType, subTypes) ->
    reflections.getSubTypesOf(parentType) shouldBe subTypes
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
