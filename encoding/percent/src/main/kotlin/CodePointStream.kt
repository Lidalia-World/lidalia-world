package uk.org.lidalia.encoding.percent

internal class CodePointStream(input: String) : StringTokenStream<CodePoint>(input) {
  override fun next(): CodePoint {
    val codePoint = input.codePointAt(index)
    index += Character.charCount(codePoint)
    return CodePoint(codePoint)
  }
}
