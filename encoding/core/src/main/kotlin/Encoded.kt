/**
 * Represents an encoded form of some value - for instance, a byte[] encoded as a hex string.
 * Should be validated as part of construction - that is, calling either method on this interface
 * should never result in an Exception.
 *
 * @param <Decoded> the type of the actual value that has been encoded (in the case of a hex string, [uk.org.lidalia.lang.Bytes])
 * @param <Raw> the type of the encoding (in the case of a hex string, a [String])
</Raw></Decoded> */
interface Encoded<Decoded, Raw> {
  /**
   * @return the original unencoded value
   */
  fun decode(): Decoded

  /**
   * @return the raw encoded form
   */
  val raw: Raw
}
