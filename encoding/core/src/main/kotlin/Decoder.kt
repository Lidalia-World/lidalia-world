package uk.org.lidalia.encoding.core

import arrow.core.Either

interface Decoder<Decoded, RawEncoded, E : Encoded<Decoded, RawEncoded>> {
  /**
   * Validates a raw encoded value is a correctly encoded form of the
   * unencoded type, and returns it wrapped in a type safe value that permits
   * decoding.
   *
   * @param encoded the raw encoded value (e.g. a hex string)
   * @return a validated and type safe wrapper around the encoded param
   * @throws InvalidEncoding if the encoded param is not a valid encoded form
   */
  fun decode(encoded: RawEncoded): Either<InvalidEncoding<Decoded, RawEncoded, E>, Decoded>

  fun validate(encoded: RawEncoded): Either<InvalidEncoding<Decoded, RawEncoded, E>, E>
}
