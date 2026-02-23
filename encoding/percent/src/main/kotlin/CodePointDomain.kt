package uk.org.lidalia.encoding.percent

import java.nio.charset.StandardCharsets.UTF_8

internal interface CodePointOrHexCodePoint : AppendableTo {
  fun decode(): CodePoint

  val isEncoded: Boolean
}

internal interface HexCodePoint : CodePointOrHexCodePoint {

  fun toUpperCase(): HexCodePoint

  val isUpperCase: Boolean

  companion object {
    fun hexDigitToInt(c: Char): Int = when (c) {
      in '0'..'9' -> c.code - '0'.code
      in 'A'..'F' -> c.code - 'A'.code + 10
      in 'a'..'f' -> c.code - 'a'.code + 10
      else -> throw IllegalArgumentException("Invalid hex digit: $c")
    }
  }
}

internal data class CodePoint(val codePoint: Int) : CodePointOrHexCodePoint {
  override val isEncoded: Boolean = false

  fun maybePercentEncode(charactersThatDoNotNeedEncoding: BooleanArray): CodePointOrHexCodePoint =
    if (isIn(charactersThatDoNotNeedEncoding)) {
      this
    } else {
      percentEncode()
    }

  private fun percentEncode(): HexCodePoint {
    val bytes = toString().toByteArray()
    val hexChars = bytes.mapToArray { it.toHexCharacter() }
    return HexSequence(hexChars)
  }

  fun isIn(charactersThatDoNotNeedEncoding: BooleanArray): Boolean =
    codePoint < charactersThatDoNotNeedEncoding.size &&
      charactersThatDoNotNeedEncoding[codePoint]

  override fun toString(): String = Character.toString(codePoint)

  override fun decode(): CodePoint = this

  override fun appendTo(builder: StringBuilder) {
    builder.appendCodePoint(codePoint)
  }

  companion object {

    private fun Byte.toHexCharacter(): HexCharacter {
      val byteValue = toInt() and 0xFF
      return HexCharacter((byteValue shr 4).toHexDigit(), (byteValue and 0x0F).toHexDigit())
    }

    fun Int.toHexDigit(): Char = (if (this < 10) '0'.code + this else 'A'.code + this - 10).toChar()
  }
}

internal class HexSequence(private val hexChars: Array<HexCharacter>) : HexCodePoint {
  init {
    require(!(hexChars.isEmpty() || hexChars.size > 4)) {
      "hex sequence must be 1-4 hex characters, was " +
        hexChars.contentToString()
    }
  }

  override fun decode(): CodePoint {
    val bytes = ByteArray(hexChars.size)
    for (i in hexChars.indices) {
      val hc = hexChars[i]
      bytes[i] = (
        (HexCodePoint.hexDigitToInt(hc.digit1) shl 4)
          or HexCodePoint.hexDigitToInt(hc.digit2)
      ).toByte()
    }

    // Convert UTF-8 bytes to String, then get the code point
    val decoded = String(bytes, UTF_8)
    val codePoint = decoded.codePointAt(0)
    return CodePoint(codePoint)
  }

  override val isEncoded: Boolean get() = true

  override fun toUpperCase(): HexSequence {
    var changed = false
    val upper: Array<HexCharacter> = Array(hexChars.size) { i ->
      val original = hexChars[i]
      val upperCase = original.toUpperCase()
      if (upperCase != original) {
        changed = true
      }
      upperCase
    }
    return if (changed) {
      HexSequence(upper)
    } else {
      this
    }
  }

  override val isUpperCase: Boolean get() {
    for (hexChar in hexChars) {
      if (!hexChar.isUpperCase) {
        return false
      }
    }
    return true
  }

  override fun toString(): String {
    val sb = StringBuilder()
    for (hexChar in hexChars) {
      sb.append(hexChar)
    }
    return sb.toString()
  }

  override fun appendTo(builder: StringBuilder) {
    for (hexChar in hexChars) {
      hexChar.appendTo(builder)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other !is HexSequence) {
      return false
    }
    return this.hexChars.contentEquals(other.hexChars)
  }

  override fun hashCode(): Int = hexChars.contentHashCode()
}

internal data class HexCharacter(val digit1: Char, val digit2: Char) : HexCodePoint {
  override fun decode(): CodePoint = CodePoint(
    (
      (HexCodePoint.hexDigitToInt(digit1) shl 4) or HexCodePoint.hexDigitToInt(
        digit2,
      )
    ).toChar().code,
  )

  override val isEncoded: Boolean get() = true

  override fun toUpperCase(): HexCharacter {
    val char1Upper = digit1.uppercaseChar()
    val char2Upper = digit2.uppercaseChar()
    return if (char1Upper == digit1 && char2Upper == digit2) {
      this
    } else {
      HexCharacter(char1Upper, char2Upper)
    }
  }

  override val isUpperCase: Boolean get() = isUpperCaseHexDigit(digit1) &&
    isUpperCaseHexDigit(digit2)

  override fun toString(): String = String(charArrayOf('%', digit1, digit2))

  override fun appendTo(builder: StringBuilder) {
    builder.append('%').append(digit1).append(digit2)
  }

  companion object {
    private fun isUpperCaseHexDigit(c: Char): Boolean =
      Character.isUpperCase(c) || Character.isDigit(c)
  }
}

private inline fun <reified B> ByteArray.mapToArray(mapper: (Byte) -> B): Array<B> =
  Array(size) { mapper(this[it]) }
