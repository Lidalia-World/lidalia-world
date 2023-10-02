abstract class EncodedBase<Decoded, Raw> protected constructor(
  override val raw: Raw,
) : Encoded<Decoded, Raw> {

  override fun toString(): String = raw.toString()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as EncodedBase<*, *>
    return raw == that.raw
  }

  override fun hashCode(): Int = raw.hashCode()
}
