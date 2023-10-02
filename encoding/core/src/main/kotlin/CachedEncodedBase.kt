abstract class CachedEncodedBase<Decoded, Raw> protected constructor(
  raw: Raw,
  private val decoded: Decoded,
) : EncodedBase<Decoded, Raw>(raw) {

  override fun decode(): Decoded = decoded
}
