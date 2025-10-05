package uk.org.lidalia.encoding.hex

import uk.org.lidalia.encoding.core.InvalidEncoding
import uk.org.lidalia.lang.Bytes

class NotAHexEncodedString(
  override val invalidEncoding: String,
) : Exception("Not a hex encoded string: [$invalidEncoding]"),
  InvalidEncoding<Bytes, String, Hex> {

  override val decoder: HexCodec = HexCodec
}
