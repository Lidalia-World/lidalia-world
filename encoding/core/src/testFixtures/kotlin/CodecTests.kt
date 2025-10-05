package uk.org.lidalia.encoding.core

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.factory.TestFactory
import io.kotest.core.spec.style.stringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

fun <Decoded, RawEncoded, E : Encoded<Decoded, RawEncoded>> codecTests(
  encoder: Codec<Decoded, RawEncoded, E>,
  instance1: Decoded,
  instance2: Decoded,
): TestFactory = stringSpec {

  val instance1A = encoder.encode(instance1)
  val instance1B = encoder.encode(instance1)
  val instance1C = encoder.encode(instance1)

  val instance2A = encoder.encode(instance2)
  val instance2B = encoder.encode(instance2)
  val instance2C = encoder.encode(instance2)

  "encoding and decoding is symmetric" {
    instance1A.decoded shouldBe instance1
    instance1B.decoded shouldBe instance1
    instance1C.decoded shouldBe instance1

    encoder.encode(instance1A.decoded) shouldBe instance1A
    encoder.encode(instance1B.decoded) shouldBe instance1A
    encoder.encode(instance1C.decoded) shouldBe instance1A

    instance1A.decoded shouldNotBe instance2
    instance1B.decoded shouldNotBe instance2
    instance1C.decoded shouldNotBe instance2

    instance2A.decoded shouldBe instance2
    instance2B.decoded shouldBe instance2
    instance2C.decoded shouldBe instance2

    instance2A.decoded shouldNotBe instance1
    instance2B.decoded shouldNotBe instance1
    instance2C.decoded shouldNotBe instance1
  }

  "encoding and decoding is symmetric using raw" {
    encoder.validate(instance1A.raw) shouldBeRight encoder.encode(instance1)
    encoder.validate(instance1B.raw) shouldBeRight encoder.encode(instance1)
    encoder.validate(instance1C.raw) shouldBeRight encoder.encode(instance1)

    encoder.encode(encoder.decode(instance1A.raw).shouldBeRight()).decoded == instance1
    encoder.encode(encoder.decode(instance1B.raw).shouldBeRight()).decoded == instance1
    encoder.encode(encoder.decode(instance1C.raw).shouldBeRight()).decoded == instance1
  }
}
