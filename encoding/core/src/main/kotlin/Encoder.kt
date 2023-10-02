/**
 * Type that allows encoding a value to some encoded form (e.g.
 * encoding some [Bytes] as [uk.org.lidalia.encoding.hex.Hex]),
 * or constructing a validated encoded form from a raw encoded form
 * (e.g. a [uk.org.lidalia.encoding.hex.Hex] from a [String]).
 *
 * @param <Decoded> the type of the actual value that has been encoded
 * (in the case of a hex string, [Bytes])
 * @param <RawEncoded> the type of the encoding (in the case of a hex string,
 * a [String])
 * @param <E> the [Encoded] type
</E></RawEncoded></Decoded> */
interface Encoder<Decoded, RawEncoded, E : Encoded<Decoded, RawEncoded>> {
  /**
   * Validates a raw encoded value is a correctly encoded form of the
   * unencoded type, and returns it wrapped in a type safe value that permits
   * decoding.
   *
   * @param encoded the raw encoded value (e.g. a hex string)
   * @return a validated and type safe wrapper around the encoded param
   * @throws InvalidEncoding if the encoded param is not a valid encoded form
   */
  @Throws(InvalidEncoding::class)
  fun of(encoded: RawEncoded): E

  /**
   * Encodes a value in some more general form
   *
   * @param decoded the original unencoded value
   * @return type safe encoded form of the value
   */
  fun encode(decoded: Decoded): E
}
