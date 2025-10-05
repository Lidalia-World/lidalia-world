package uk.org.lidalia.encoding.hex

import uk.org.lidalia.encoding.core.Encoded
import uk.org.lidalia.lang.Bytes

@ConsistentCopyVisibility
data class Hex internal constructor(
  override val raw: String,
  override val decoded: Bytes,
) : Encoded<Bytes, String> {
  override fun toString(): String = raw
}
