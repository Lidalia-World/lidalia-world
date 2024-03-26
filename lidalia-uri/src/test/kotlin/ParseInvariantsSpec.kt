package uk.org.lidalia.uri

import arrow.core.getOrElse
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import uk.org.lidalia.lang.CharSequenceParser
import uk.org.lidalia.uri.api.AbsoluteUrl
import uk.org.lidalia.uri.api.AbsoluteUrn
import uk.org.lidalia.uri.api.PathEmpty
import uk.org.lidalia.uri.api.PathNoScheme
import uk.org.lidalia.uri.api.PathRootless
import uk.org.lidalia.uri.api.RelativeRef
import uk.org.lidalia.uri.api.Url
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.superclasses

class ParseInvariantsSpec : StringSpec(
  {

    val testCases = listOf(
      "s:" to AbsoluteUrn::class,
      "s:foo:bar" to AbsoluteUrn::class,

      "s:b/foo" to AbsoluteUrn::class,
      "s:b:c/foo" to AbsoluteUrn::class,

      "p1:p2/p3" to PathRootless::class,

      "p1/p2:p3" to PathNoScheme::class,

      "s://h/p2" to AbsoluteUrl::class,
      "s://h/p2?" to AbsoluteUrl::class,
      "s://h/p2?q" to AbsoluteUrl::class,

      "s://h/p2#" to Url::class,
      "s://h/p2#f" to Url::class,
      "s://h/p2?#" to Url::class,
      "s://h/p2?q#" to Url::class,
      "s://h/p2?#f" to Url::class,
      "s://h/p2?q#f" to Url::class,

      "//h/p2#f" to RelativeRef::class,
      "/p2#f" to RelativeRef::class,
      "" to PathEmpty::class,
    )

    withData<ParseTestCase>(
      { t -> "[${t.stringForm}] can be parsed to ${t.expectedType.simpleName} symmetrically" },
      testCases.toParseTestCases(),
    ) { (stringForm, expectedType) ->
      val parser = expectedType.parser ?: throw AssertionError("No parser for $expectedType")
      val parsed = parser(stringForm).getOrElse { throw it }
      parsed::class.superclasses shouldContain expectedType
      parsed.toString() shouldBe stringForm
    }

    withData<UnambiguousParseTestCase>(
      { t -> "[${t.stringForm}] is parsed to ${t.expectedType.simpleName} by ${t.parser}" },
      testCases.toUnambiguousParseTestCases(),
    ) { (stringForm, expectedType, parser) ->
      val parsed = parser(stringForm).getOrElse { throw it }
      parsed::class.superclasses shouldContain expectedType
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
