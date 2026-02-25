package uk.org.lidalia.encoding.percent

internal class Constants private constructor() {
  init {
    throw UnsupportedOperationException("Not instantiable")
  }

  companion object {
    val empty: BooleanArray = BooleanArray(0)

    fun include(vararg chars: Char): BooleanArray {
      val charSet = BooleanArray(128)
      for (aChar in chars) {
        charSet[aChar.code] = true
      }
      return charSet
    }
  }
}
