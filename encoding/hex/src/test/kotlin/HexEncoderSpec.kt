import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.RandomStringUtils
import uk.org.lidalia.encoding.core.codecTests
import uk.org.lidalia.encoding.hex.HexCodec
import uk.org.lidalia.lang.Bytes
import uk.org.lidalia.lang.Bytes.Companion.toBytes

class HexEncoderSpec : StringSpec({
  include(
    codecTests(
      encoder = HexCodec,
      instance1 = Bytes.random(length = 500),
      instance2 = Bytes.random(length = 500),
    ),
  )
  listOf(
    "00017F80FF",
    "00017f80ff",
  ).forEach { hexString ->
    "raw encoded form $hexString is case insensitive" {

      val hex = HexCodec.validate(hexString).shouldBeRight()

      hex.raw shouldBe hexString
      hex.toString() shouldBe hexString
      hex.decoded shouldBe byteArrayOf(0, 1, 127, -128, -1).toBytes()
    }
  }

  "handles empty forms" {
    HexCodec.decode("") shouldBeRight Bytes.empty
    HexCodec.validate("") shouldBeRight HexCodec.encode(Bytes.empty)
  }

  listOf(
    "G",
    "GG",
    "-10",
    RandomStringUtils.insecure().next(16),
  ).forEach { invalidHex ->
    val invalidEncoding = HexCodec.validate(invalidHex).shouldBeLeft()
    invalidEncoding.message shouldBe "Not a hex encoded string: [$invalidHex]"
    invalidEncoding.invalidEncoding shouldBe invalidHex
  }
})
