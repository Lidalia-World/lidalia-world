package uk.org.lidalia.encoding.percent

import uk.org.lidalia.encoding.core.Encoded
import uk.org.lidalia.lang.Bytes

@ConsistentCopyVisibility
data class PercentEncoded internal constructor(
  override val raw: String,
  override val decoded: String,
) : Encoded<String, String> {
  override fun toString(): String = raw
}
