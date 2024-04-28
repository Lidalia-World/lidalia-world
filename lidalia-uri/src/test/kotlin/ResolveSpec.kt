import arrow.core.Either
import arrow.core.getOrElse
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import uk.org.lidalia.uri.api.Path
import uk.org.lidalia.uri.api.UriReference
import uk.org.lidalia.uri.api.toUriReference

class ResolveSpec : StringSpec(
  {
    withData<UriReferenceResolveUriReferenceTestCase>(
      { (base, toResolve, expected) -> "$base.resolve($toResolve) should be $expected" },
      listOf(
        testCase("s:", "s:", "s:"),
        testCase("s:", "s:p1", "s:p1"),
        testCase("s:", "s:p1#", "s:p1#"),
        testCase("s:", "s:p1#f", "s:p1#f"),
        testCase("s:", "s:p1?", "s:p1?"),
        testCase("s:", "s:p1?q", "s:p1?q"),
        testCase("s:", "s:p1?q#", "s:p1?q#"),
        testCase("s:", "s:p1?#f", "s:p1?#f"),
        testCase("s:", "s:p1?q#f", "s:p1?q#f"),
        testCase("s:", "s://h/p2", "s://h/p2"),
        testCase("s:", "s://h/p2?", "s://h/p2?"),
        testCase("s:", "s://h/p2?q", "s://h/p2?q"),

        testCase("s:p1", "s:", "s:"),
        testCase("s:p1", "s:p1", "s:p1"),
        testCase("s:p1", "s:p1#", "s:p1#"),
        testCase("s:p1", "s:p1#f", "s:p1#f"),
        testCase("s:p1", "s:p1?", "s:p1?"),
        testCase("s:p1", "s:p1?q", "s:p1?q"),
        testCase("s:p1", "s:p1?q#", "s:p1?q#"),
        testCase("s:p1", "s:p1?#f", "s:p1?#f"),
        testCase("s:p1", "s:p1?q#f", "s:p1?q#f"),

        testCase("//h/p2#", "s:", "s:"),
        testCase("//h/p2#", "s:p1", "s:p1"),
        testCase("//h/p2#", "s:p1#", "s:p1#"),
        testCase("//h/p2#", "s:p1#f", "s:p1#f"),
        testCase("//h/p2#", "s:p1?", "s:p1?"),
        testCase("//h/p2#", "s:p1?q", "s:p1?q"),
        testCase("//h/p2#", "s:p1?q#", "s:p1?q#"),
        testCase("//h/p2#", "s:p1?#f", "s:p1?#f"),
        testCase("//h/p2#", "s:p1?q#f", "s:p1?q#f"),
        testCase("//h/p2#", "s://h/p2", "s://h/p2"),
        testCase("//h/p2#", "s://h/p2?", "s://h/p2?"),
        testCase("//h/p2#", "s://h/p2?q", "s://h/p2?q"),
      ),
    ) { (base, toResolve, expected) ->
      base.resolve(toResolve) shouldBe expected
    }
  },
)

data class UriReferenceResolveUriReferenceTestCase(
  val base: UriReference,
  val toResolve: UriReference,
  val expected: UriReference,
)

fun testCase(
  base: String,
  toResolve: String,
  expected: String,
) = UriReferenceResolveUriReferenceTestCase(
  base.toUriReference().orThrow(),
  toResolve.toUriReference().orThrow(),
  expected.toUriReference().orThrow(),
)

data class UriReferenceResolvePathTestCase(
  val base: UriReference,
  val toResolve: Path,
  val expected: UriReference,
)

fun testCase(
  base: UriReference,
  toResolve: Path,
  expected: UriReference,
) = UriReferenceResolvePathTestCase(
  base,
  toResolve,
  expected,
)

data class PathResolvePathTestCase(
  val base: Path,
  val toResolve: Path,
  val expected: Path,
)

fun testCase(
  base: Path,
  toResolve: Path,
  expected: Path,
) = PathResolvePathTestCase(
  base,
  toResolve,
  expected,
)

fun <T : Exception, A> Either<T, A>.orThrow() = getOrElse { throw it }
