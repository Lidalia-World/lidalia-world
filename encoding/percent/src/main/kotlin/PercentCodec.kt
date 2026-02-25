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
    val result = decoded.codePoints().asSequence().joinToString("") {
      CodePoint(it).maybePercentEncode(charactersThatDoNotNeedEncoding).toString()
    }
    return PercentEncoded(result, decoded)
  }

  override fun decode(encoded: String): Right<String> = Right(rawDecode(encoded))

  override fun validate(
    encoded: String,
  ): Either<InvalidEncoding<String, String, PercentEncoded>, PercentEncoded> =
    Right(PercentEncoded(encoded, rawDecode(encoded)))

  private fun rawDecode(encoded: String): String {
    val result = StringBuilder()
    PercentEncodedStream(encoded).forEach { it.appendTo(result) }
    return result.toString()
  }
}
