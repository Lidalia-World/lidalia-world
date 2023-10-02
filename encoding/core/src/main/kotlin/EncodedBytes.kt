import uk.org.lidalia.lang.Bytes

interface EncodedBytes : Encoded<Bytes, String> {
  override fun decode(): Bytes
}
