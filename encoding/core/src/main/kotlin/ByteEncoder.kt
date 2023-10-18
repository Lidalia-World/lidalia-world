import uk.org.lidalia.lang.Bytes
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

interface ByteEncoder<T : Encoded<Bytes, String>> : Encoder<Bytes, String, T> {

  fun encode(decoded: ByteArray): T = encode(Bytes(decoded))

  fun encode(decoded: String, charset: Charset = StandardCharsets.UTF_8): T =
    encode(Bytes(decoded, charset))
}
