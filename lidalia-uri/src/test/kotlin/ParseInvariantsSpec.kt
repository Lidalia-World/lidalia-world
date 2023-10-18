package uk.org.lidalia.uri

import arrow.core.getOrElse
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import uk.org.lidalia.lang.CharSequenceParser
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance

class ParseInvariantsSpec : StringSpec(
  {

    val testCases = listOf(
      "g:" to AbsoluteUri::class,
      "g:foo:bar" to AbsoluteUri::class,

      "g:b/foo" to AbsoluteUri::class,
      "g:b:c/foo" to AbsoluteUri::class,

      "g:b/foo" to PathRootless::class,

      "g/foo:bar" to PathNoScheme::class,

      "https://example.com/foo" to AbsoluteUrl::class,
      "https://example.com/foo?" to AbsoluteUrl::class,
      "https://example.com/foo?a" to AbsoluteUrl::class,

      "https://example.com/foo#" to UrlWithFragment::class,
      "https://example.com/foo#f" to UrlWithFragment::class,
      "https://example.com/foo?#" to UrlWithFragment::class,
      "https://example.com/foo?a#" to UrlWithFragment::class,
      "https://example.com/foo?#f" to UrlWithFragment::class,
      "https://example.com/foo?a#f" to UrlWithFragment::class,

      "//example.com/foo#f" to RelativePartWithAuthority::class,
      "/foo#f" to RelativePart::class,
      "" to PathEmpty::class,
    )

    withData<ParseTestCase>(
      { t -> "[${t.stringForm}] can be parsed to ${t.expectedType.simpleName} symmetrically" },
      testCases.toParseTestCases(),
    ) { (stringForm, expectedType) ->
      val parsed = expectedType.parser!!(stringForm).getOrElse { throw it }
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
      superClass.parser?.let {
          parser ->
        UnambiguousParseTestCase(stringForm, expectedType, parser)
      }
    }
  }

data class ParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out Any>,
)

data class UnambiguousParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out Any>,
  val parser: CharSequenceParser<Exception, Any>,
)
