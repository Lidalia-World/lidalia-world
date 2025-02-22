package lidalia.kotest

import io.kotest.matchers.concurrent.shouldCompleteWithin
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.time.Duration

fun <T> Future<T>.shouldCompleteWithin(timeout: Duration): T =
  shouldCompleteWithin(timeout.inWholeMilliseconds, MILLISECONDS) { get() }
