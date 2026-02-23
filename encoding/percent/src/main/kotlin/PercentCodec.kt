package uk.org.lidalia.encoding.percent

import arrow.core.Either
import arrow.core.Either.Right
import uk.org.lidalia.encoding.core.Codec
import uk.org.lidalia.encoding.core.InvalidEncoding
import kotlin.streams.asSequence

class PercentCodec private constructor(
  private val charactersThatDoNotNeedEncoding: BooleanArray,
) : Codec<String, String, PercentEncoded> {

  override fun encode(decoded: String): PercentEncoded {
    val result = AppendableToAwareStringBuilder()
    decoded.codePoints().asSequence().joinToString("") {
      CodePoint(it).maybePercentEncode(charactersThatDoNotNeedEncoding).toString()
    }
    CodePointStream(decoded).forEach { codePoint: CodePoint ->
      result.append(
        codePoint.maybePercentEncode(
          charactersThatDoNotNeedEncoding,
        ),
      )
    }
    return PercentEncoded(result.toString(), decoded)
  }

  override fun decode(encoded: String): Right<String> = Right(rawDecode(encoded))

  override fun validate(
    encoded: String,
  ): Either<InvalidEncoding<String, String, PercentEncoded>, PercentEncoded> =
    Right(PercentEncoded(encoded, rawDecode(encoded)))

  private fun rawDecode(encoded: String): String {
    val result = AppendableToAwareStringBuilder()
    val percentEncodedStream = PercentEncodedStream(encoded)
    while (percentEncodedStream.hasNext()) {
      result.append(percentEncodedStream.next().decode())
    }
    val value = result.toString()
    return value
  }
}
