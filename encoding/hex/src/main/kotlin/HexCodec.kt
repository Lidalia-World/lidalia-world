package uk.org.lidalia.encoding.hex

import arrow.core.Either
import arrow.core.raise.either
import uk.org.lidalia.encoding.core.Codec
import uk.org.lidalia.encoding.core.InvalidEncoding
import uk.org.lidalia.lang.Bytes
import uk.org.lidalia.lang.Bytes.Companion.toBytes

object HexCodec : Codec<Bytes, String, Hex> {

  override fun encode(decoded: Bytes): Hex {
    val decodedByes = decoded.array()
    val chars = CharArray(decodedByes.size * 2)
    for (i in decodedByes.indices) {
      val nibble1 = (0xF0 and decodedByes[i].toInt()) ushr 4
      val nibble2 = 0x0F and decodedByes[i].toInt()
      chars[i * 2] = Character.forDigit(nibble1, 16)
      chars[i * 2 + 1] = Character.forDigit(nibble2, 16)
    }
    return Hex(String(chars), decoded)
  }

  override fun decode(encoded: String): Either<InvalidEncoding<Bytes, String, Hex>, Bytes> =
    either<NotAHexEncodedString, Bytes> {
      val chars = encoded.toCharArray()
      if (chars.size % 2 != 0) {
        raise(NotAHexEncodedString(encoded))
      }
      val decoded = ByteArray(chars.size / 2)

      for (i in decoded.indices) {
        val nibble1 = chars[i * 2].digitToIntOrNull(16) ?: -1
        val nibble2 = chars[i * 2 + 1].digitToIntOrNull(16) ?: -1
        if (nibble1 == -1 || nibble2 == -1) {
          raise(NotAHexEncodedString(encoded))
        }
        decoded[i] = (nibble1 * 16 + nibble2).toByte()
      }

      decoded.toBytes()
    }

  override fun validate(encoded: String): Either<InvalidEncoding<Bytes, String, Hex>, Hex> =
    decode(encoded).map { bytes -> Hex(encoded, bytes) }
}

val hex: HexCodec = HexCodec
