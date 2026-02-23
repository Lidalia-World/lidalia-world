package uk.org.lidalia.encoding.percent

internal class PercentEncodedStream(input: String) :
  StringTokenStream<CodePointOrHexCodePoint>(input) {
  override fun next(): CodePointOrHexCodePoint {
    // Check percent-encoded sequences
    val maybeHex = nextHex(index)
    if (maybeHex != null) {
      // Decode the first byte to check if it's a multibyte UTF-8 sequence
      val decodedChar = maybeHex.decode().codePoint
      val firstByte = decodedChar and 0xFF
      val utf8Length: Int = getUtf8SequenceLength(firstByte)

      if (utf8Length == 1) {
        index += 3
        return maybeHex
      } else {
        // Multibyte UTF-8 sequence - read the continuation bytes
        val hexChars = arrayOfNulls<HexCharacter>(utf8Length)
        hexChars[0] = maybeHex

        for (i in 1..<utf8Length) {
          val continuationHex = nextHex(index + (i * 3))
          if (continuationHex == null) {
            // Invalid UTF-8 sequence - treat first byte as standalone
            index += 3
            return maybeHex
          }
          hexChars[i] = continuationHex
        }

        index += utf8Length * 3
        return HexSequence(hexChars as Array<HexCharacter>)
      }
    }
    val codePoint = input.codePointAt(index)
    index += Character.charCount(codePoint)
    return CodePoint(codePoint)
  }

  private fun nextHex(startPoint: Int): HexCharacter? {
    if (startPoint >= input.length) {
      return null
    }
    val startChar = input[startPoint]
    if (startChar == '%' && startPoint + 2 < input.length) {
      val maybeFirstHexDigit = input[startPoint + 1]
      val maybeSecondHexDigit = input[startPoint + 2]
      if (isHexDigit(maybeFirstHexDigit) && isHexDigit(maybeSecondHexDigit)) {
        return HexCharacter(maybeFirstHexDigit, maybeSecondHexDigit)
      }
    }
    return null
  }

  companion object {
    // Check the high bits to determine UTF-8 sequence length
    private fun getUtf8SequenceLength(firstByte: Int): Int = when {
      // 0xxxxxxx = 1 byte (ASCII)
      (firstByte and 0x80) == 0 -> 1
      // 110xxxxx = 2 bytes
      (firstByte and 0xE0) == 0xC0 -> 2
      // 1110xxxx = 3 bytes
      (firstByte and 0xF0) == 0xE0 -> 3
      // 11110xxx = 4 bytes
      (firstByte and 0xF8) == 0xF0 -> 4
      // Invalid UTF-8 lead byte - treat as single byte
      else -> 1
    }

    private fun isHexDigit(c: Char): Boolean = (c in '0'..'9') || (c in 'A'..'F') || (c in 'a'..'f')
  }
}
