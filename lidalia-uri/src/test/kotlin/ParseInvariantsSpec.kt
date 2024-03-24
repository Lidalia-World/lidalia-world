package uk.org.lidalia.uri

import arrow.core.getOrElse
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.api.PathNoScheme
import uk.org.lidalia.uri.api.PathRootless
import uk.org.lidalia.uri.api.RelativePart
import uk.org.lidalia.uri.api.RelativePartWithAuthority
import uk.org.lidalia.uri.implementation.BasicAbsoluteUrl
import uk.org.lidalia.uri.implementation.BasicAbsoluteUrn
import uk.org.lidalia.uri.implementation.BasicPathEmpty
import uk.org.lidalia.uri.implementation.BasicUrl
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance

class ParseInvariantsSpec : StringSpec(
  {

    val testCases = listOf(
      "g:" to BasicAbsoluteUrn::class,
      "g:foo:bar" to BasicAbsoluteUrn::class,

      "g:b/foo" to BasicAbsoluteUrn::class,
      "g:b:c/foo" to BasicAbsoluteUrn::class,

      "g:b/foo" to PathRootless::class,

      "g/foo:bar" to PathNoScheme::class,

      "https://example.com/foo" to BasicAbsoluteUrl::class,
      "https://example.com/foo?" to BasicAbsoluteUrl::class,
      "https://example.com/foo?a" to BasicAbsoluteUrl::class,

      "https://example.com/foo#" to BasicUrl::class,
      "https://example.com/foo#f" to BasicUrl::class,
      "https://example.com/foo?#" to BasicUrl::class,
      "https://example.com/foo?a#" to BasicUrl::class,
      "https://example.com/foo?#f" to BasicUrl::class,
      "https://example.com/foo?a#f" to BasicUrl::class,

      "//example.com/foo#f" to RelativePartWithAuthority::class,
      "/foo#f" to RelativePart::class,
      "" to BasicPathEmpty::class,
    )

    withData<ParseTestCase>(
      { t -> "[${t.stringForm}] can be parsed to ${t.expectedType.simpleName} symmetrically" },
      testCases.toParseTestCases(),
    ) { (stringForm, expectedType) ->
      val parser = expectedType.parser ?: throw AssertionError("No parser for $expectedType")
      val parsed = parser(stringForm).getOrElse { throw it }
      parsed::class shouldBe expectedType
      parsed.toString() shouldBe stringForm
    }

    withData<UnambiguousParseTestCase>(
      { t -> "[${t.stringForm}] is parsed to ${t.expectedType.simpleName} by ${t.parser}" },
      testCases.toUnambiguousParseTestCases(),
    ) { (stringForm, expectedType, parser) ->
      val parsed = parser(stringForm).getOrElse { throw it }
      parsed::class shouldBe expectedType
    }
  },
)

@Suppress("UNCHECKED_CAST")
private val <T : Any> KClass<T>.parser get() =
  (companionObjectInstance ?: objectInstance) as CharSequenceParser<Exception, T>?

fun List<Pair<String, KClass<out Any>>>.toParseTestCases() = map {
  ParseTestCase(it.first, it.second)
}

fun List<Pair<String, KClass<out Any>>>.toUnambiguousParseTestCases() =
  flatMap { (stringForm, expectedType) ->
    expectedType.allSuperclasses.mapNotNull { superClass ->
      superClass.parser?.unambiguousParseTestCase(stringForm, expectedType)
    }
  }

private fun CharSequenceParser<Exception, Any>.unambiguousParseTestCase(
  stringForm: String,
  expectedType: KClass<out Any>,
) = UnambiguousParseTestCase(stringForm, expectedType, this)

data class ParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out Any>,
)

data class UnambiguousParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out Any>,
  val parser: CharSequenceParser<Exception, Any>,
)
