package uk.org.lidalia.uri

import arrow.core.getOrElse
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
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
import uk.org.lidalia.uri.api.toAuthority
import uk.org.lidalia.uri.api.toFragment
import uk.org.lidalia.uri.api.toPath
import uk.org.lidalia.uri.api.toQuery
import uk.org.lidalia.uri.api.toScheme
import uk.org.lidalia.uri.implementation.BasicSegment
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.superclasses

class ParseInvariantsSpec : StringSpec(
  {

    val uriReferenceTestCases = listOf(
      testCase<AbsoluteUrn>("s:", expectation("s", null, "", null, null)),
      testCase<AbsoluteUrn>("s:p1", expectation("s", null, "p1", null, null)),
      testCase<AbsoluteUrn>("s:p:1", expectation("s", null, "p:1", null, null)),
      testCase<AbsoluteUrn>("s:b/foo", expectation("s", null, "b/foo", null, null)),
      testCase<AbsoluteUrn>("s:b:c/foo", expectation("s", null, "b:c/foo", null, null)),
      testCase<Urn>("s:p1#", expectation("s", null, "p1", null, "")),
      testCase<Urn>("s:p1#f", expectation("s", null, "p1", null, "f")),
      testCase<Urn>("s:p1?#", expectation("s", null, "p1", "", "")),
      testCase<Urn>("s:p1?q#", expectation("s", null, "p1", "q", "")),
      testCase<Urn>("s:p1?#f", expectation("s", null, "p1", "", "f")),
      testCase<Urn>("s:p1?q#f", expectation("s", null, "p1", "q", "f")),
      testCase<AbsoluteUrl>("s://h/p2", expectation("s", "h", "/p2", null, null)),
      testCase<AbsoluteUrl>("s://h/p2?", expectation("s", "h", "/p2", "", null)),
      testCase<AbsoluteUrl>("s://h/p2?q", expectation("s", "h", "/p2", "q", null)),
      testCase<Url>("s://h/p2#", expectation("s", "h", "/p2", null, "")),
      testCase<Url>("s://h/p2#f", expectation("s", "h", "/p2", null, "f")),
      testCase<Url>("s://h/p2?#", expectation("s", "h", "/p2", "", "")),
      testCase<Url>("s://h/p2?q#", expectation("s", "h", "/p2", "q", "")),
      testCase<Url>("s://h/p2?#f", expectation("s", "h", "/p2", "", "f")),
      testCase<Url>("s://h/p2?q#f", expectation("s", "h", "/p2", "q", "f")),
      testCase<RelativeRef>("//h/p2#", expectation(null, "h", "/p2", null, "")),
      testCase<RelativeRef>("//h/p2#f", expectation(null, "h", "/p2", null, "f")),
      testCase<RelativeRef>("//h/p2?#", expectation(null, "h", "/p2", "", "")),
      testCase<RelativeRef>("//h/p2?q#", expectation(null, "h", "/p2", "q", "")),
      testCase<RelativeRef>("//h/p2?#f", expectation(null, "h", "/p2", "", "f")),
      testCase<RelativeRef>("//h/p2?q#f", expectation(null, "h", "/p2", "q", "f")),
      testCase<RelativeRef>("/p2/p3#", expectation(null, null, "/p2/p3", null, "")),
      testCase<RelativeRef>("/p2/p3#f", expectation(null, null, "/p2/p3", null, "f")),
      testCase<RelativeRef>("/p2/p3?#", expectation(null, null, "/p2/p3", "", "")),
      testCase<RelativeRef>("/p2/p3?q#", expectation(null, null, "/p2/p3", "q", "")),
      testCase<RelativeRef>("/p2/p3?#f", expectation(null, null, "/p2/p3", "", "f")),
      testCase<RelativeRef>("/p2/p3?q#f", expectation(null, null, "/p2/p3", "q", "f")),
      testCase<RelativeRef>("p1/p2#", expectation(null, null, "p1/p2", null, "")),
      testCase<RelativeRef>("p1/p2#f", expectation(null, null, "p1/p2", null, "f")),
      testCase<RelativeRef>("p1/p2?#", expectation(null, null, "p1/p2", "", "")),
      testCase<RelativeRef>("p1/p2?q#", expectation(null, null, "p1/p2", "q", "")),
      testCase<RelativeRef>("p1/p2?#f", expectation(null, null, "p1/p2", "", "f")),
      testCase<RelativeRef>("p1/p2?q#f", expectation(null, null, "p1/p2", "q", "f")),
      testCase<RelativeRef>("p1#", expectation(null, null, "p1", null, "")),
      testCase<RelativeRef>("p1#f", expectation(null, null, "p1", null, "f")),
      testCase<RelativeRef>("p1?#", expectation(null, null, "p1", "", "")),
      testCase<RelativeRef>("p1?q#", expectation(null, null, "p1", "q", "")),
      testCase<RelativeRef>("p1?#f", expectation(null, null, "p1", "", "f")),
      testCase<RelativeRef>("p1?q#f", expectation(null, null, "p1", "q", "f")),

      testCase<RelativeRef>("", expectation(null, null, "", null, null)),
      testCase<RelativeRef>("p1", expectation(null, null, "p1", null, null)),
      testCase<RelativeRef>("p1/p2/p3", expectation(null, null, "p1/p2/p3", null, null)),
      testCase<RelativeRef>("p1/", expectation(null, null, "p1/", null, null)),
      testCase<RelativeRef>("p1//", expectation(null, null, "p1//", null, null)),
      testCase<RelativeRef>("p1/p2/", expectation(null, null, "p1/p2/", null, null)),
      testCase<RelativeRef>("p1//p3", expectation(null, null, "p1//p3", null, null)),
      testCase<RelativeRef>("?", expectation(null, null, "", "", null)),
      testCase<RelativeRef>("?q", expectation(null, null, "", "q", null)),

      testCase<PathAndQuery>("/", expectation(null, null, "/", null, null)),
      testCase<PathAndQuery>("/p2", expectation(null, null, "/p2", null, null)),
      testCase<PathAndQuery>("/p2/", expectation(null, null, "/p2/", null, null)),
      testCase<PathAndQuery>("/p2/p3", expectation(null, null, "/p2/p3", null, null)),
      testCase<PathAndQuery>("/?", expectation(null, null, "/", "", null)),
      testCase<PathAndQuery>("/?q", expectation(null, null, "/", "q", null)),
      testCase<PathAndQuery>("/p2?", expectation(null, null, "/p2", "", null)),
      testCase<PathAndQuery>("/p2?q", expectation(null, null, "/p2", "q", null)),
      testCase<PathAndQuery>("/p2/?", expectation(null, null, "/p2/", "", null)),
      testCase<PathAndQuery>("/p2/?q", expectation(null, null, "/p2/", "q", null)),
      testCase<PathAndQuery>("/p2/p3?", expectation(null, null, "/p2/p3", "", null)),
      testCase<PathAndQuery>("/p2/p3?q", expectation(null, null, "/p2/p3", "q", null)),

      testCase<AbsoluteUrn>("s:1/p2/p3", expectation("s", null, "1/p2/p3", null, null)),
      testCase<AbsoluteUrn>("s:1/", expectation("s", null, "1/", null, null)),
      testCase<AbsoluteUrn>("s:1//", expectation("s", null, "1//", null, null)),
      testCase<AbsoluteUrn>("s:1/p2/", expectation("s", null, "1/p2/", null, null)),
      testCase<AbsoluteUrn>("s:1//p3", expectation("s", null, "1//p3", null, null)),
      testCase<RelativeRef>("//", expectation(null, "", "", null, null)),
      testCase<RelativeRef>("//p3", expectation(null, "p3", "", null, null)),
    )

    val pathTestCases = listOf(
      testCase<Path>("", listOf(""), true, false),
      testCase<Path>("p1", listOf("p1"), false, false),
      testCase<Path>("p1/p2/p3", listOf("p1", "p2", "p3"), false, false),
      testCase<Path>("p1/", listOf("p1", ""), false, false),
      testCase<Path>("p1//", listOf("p1", "", ""), false, false),
      testCase<Path>("p1/p2/", listOf("p1", "p2", ""), false, false),
      testCase<Path>("p1//p3", listOf("p1", "", "p3"), false, false),
      testCase<Path>("/", listOf("", ""), false, true),
      testCase<Path>("/p2", listOf("", "p2"), false, true),
      testCase<Path>("/p2/", listOf("", "p2", ""), false, true),
      testCase<Path>("/p2/p3", listOf("", "p2", "p3"), false, true),
      testCase<Path>("p:1", listOf("p:1"), false, false),
      testCase<Path>("p:1/p2/p3", listOf("p:1", "p2", "p3"), false, false),
      testCase<Path>("p:1/", listOf("p:1", ""), false, false),
      testCase<Path>("p:1//", listOf("p:1", "", ""), false, false),
      testCase<Path>("p:1/p2/", listOf("p:1", "p2", ""), false, false),
      testCase<Path>("p:1//p3", listOf("p:1", "", "p3"), false, false),
      testCase<Path>("//", listOf("", "", ""), false, true),
      testCase<Path>("//p3", listOf("", "", "p3"), false, true),
    )

    withData<UnambiguousUriRefParseTestCase>(
      { t -> "[${t.stringForm}] is parsed to ${t.expectedType.simpleName} by ${t.parser}" },
      uriReferenceTestCases.toUnambiguousUriRefParseTestCases(),
    ) { (stringForm, expectedType, expectation, parser) ->
      val parsed = parser(stringForm).getOrElse { throw it }
      parsed::class.superclasses shouldContain expectedType
      parsed.toString() shouldBe stringForm

      parsed.scheme shouldBe expectation.scheme
      parsed.authority shouldBe expectation.authority
      parsed.path shouldBe expectation.path
      parsed.query shouldBe expectation.query
      parsed.fragment shouldBe expectation.fragment
    }

    withData<UnambiguousPathParseTestCase>(
      { t -> "[${t.stringForm}] is parsed to ${t.expectedType.simpleName} by ${t.parser}" },
      pathTestCases.toUnambiguousPathParseTestCases(),
    ) { (stringForm, expectedType, expectedSegments, empty, absolute, parser) ->
      val parsed = parser(stringForm).getOrElse { throw it }
      parsed::class.superclasses shouldContain expectedType
      parsed.segments shouldBe expectedSegments.map { BasicSegment(it) }
      parsed.isEmpty shouldBe empty
      parsed.isAbsolute shouldBe absolute
      parsed.toString() shouldBe stringForm
    }
  },
)

@Suppress("UNCHECKED_CAST")
private val <T : Any> KClass<T>.parser
  get() =
    (companionObjectInstance ?: objectInstance) as CharSequenceParser<Exception, T>?

@Suppress("UNCHECKED_CAST")
fun List<UriReferenceParseTestCase>.toUnambiguousUriRefParseTestCases() = flatMap { testCase ->
  (testCase.expectedType.allSuperclasses + testCase.expectedType).mapNotNull { superClass ->
    val parser = superClass.parser as CharSequenceParser<Exception, UriReference>?
    parser?.unambiguousUriRefParseTestCase(testCase)
  }
}

private fun CharSequenceParser<Exception, UriReference>.unambiguousUriRefParseTestCase(
  uriReferenceParseTestCase: UriReferenceParseTestCase,
) = UnambiguousUriRefParseTestCase(
  uriReferenceParseTestCase.stringForm,
  uriReferenceParseTestCase.expectedType,
  uriReferenceParseTestCase.expectation,
  this,
)

data class UnambiguousUriRefParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out UriReference>,
  val expectation: UriReferenceExpectation,
  val parser: CharSequenceParser<Exception, UriReference>,
)

inline fun <reified T : UriReference> testCase(
  stringForm: String,
  expectation: UriReferenceExpectation,
) = UriReferenceParseTestCase(stringForm, T::class, expectation)

data class UriReferenceParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out UriReference>,
  val expectation: UriReferenceExpectation,
)

fun expectation(
  scheme: String?,
  authority: String?,
  path: String,
  query: String?,
  fragment: String?,
) = UriReferenceExpectation(
  scheme?.toScheme()?.getOrElse { throw it },
  authority?.toAuthority()?.getOrElse { throw it },
  path.toPath().getOrElse { throw it },
  query?.toQuery()?.getOrElse { throw it },
  fragment?.toFragment()?.getOrElse { throw it },
)

data class UriReferenceExpectation(
  val scheme: Scheme?,
  val authority: Authority?,
  val path: Path,
  val query: Query?,
  val fragment: Fragment?,
)

inline fun <reified T : Path> testCase(
  stringForm: String,
  expectedSegments: List<String>,
  shouldBeEmpty: Boolean,
  shouldBeAbsolute: Boolean,
) = PathParseTestCase(
  stringForm,
  T::class,
  expectedSegments,
  shouldBeEmpty,
  shouldBeAbsolute,
)

data class PathParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out Path>,
  val expectedSegments: List<String>,
  val shouldBeEmpty: Boolean,
  val shouldBeAbsolute: Boolean,
)

data class UnambiguousPathParseTestCase(
  val stringForm: String,
  val expectedType: KClass<out Path>,
  val expectedSegments: List<String>,
  val shouldBeEmpty: Boolean,
  val shouldBeAbsolute: Boolean,
  val parser: CharSequenceParser<Exception, Path>,
)

@Suppress("UNCHECKED_CAST")
fun List<PathParseTestCase>.toUnambiguousPathParseTestCases() = flatMap { testCase ->
  (testCase.expectedType.allSuperclasses + testCase.expectedType).mapNotNull { superClass ->
    (superClass.parser as CharSequenceParser<Exception, Path>?)?.unambiguousPathParseTestCase(
      testCase,
    )
  }
}

private fun CharSequenceParser<Exception, Path>.unambiguousPathParseTestCase(
  pathParseTestCase: PathParseTestCase,
) = UnambiguousPathParseTestCase(
  pathParseTestCase.stringForm,
  pathParseTestCase.expectedType,
  pathParseTestCase.expectedSegments,
  pathParseTestCase.shouldBeEmpty,
  pathParseTestCase.shouldBeAbsolute,
  this,
)
