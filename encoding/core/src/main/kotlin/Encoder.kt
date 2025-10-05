package uk.org.lidalia.encoding.core

/**
 * Type that allows encoding a value to some encoded form (e.g.
 * encoding some [uk.org.lidalia.lang.Bytes] as [uk.org.lidalia.encoding.hex.Hex]).
 *
 * @param <Decoded> the type of the actual value to be encoded
 * (in the case of a hex string, [uk.org.lidalia.lang.Bytes])
 * @param <RawEncoded> the type of the encoding (in the case of a hex string,
 * a [String])
 * @param <E> the [Encoded] type
 */
interface Encoder<Decoded, RawEncoded, E : Encoded<Decoded, RawEncoded>> {
  /**
   * Encodes a value in some more general form
   *
   * @param decoded the original unencoded value
   * @return type safe encoded form of the value
   */
  fun encode(decoded: Decoded): E
}
