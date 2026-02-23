package uk.org.lidalia.encoding.percent

import java.util.stream.IntStream

internal interface AppendableTo {
  fun appendTo(builder: StringBuilder)
}

@Suppress("unused")
internal class AppendableToAwareStringBuilder(
  private val delegate: StringBuilder = StringBuilder(),
) : CharSequence by delegate {

  fun append(appendableTo: AppendableTo): AppendableToAwareStringBuilder {
    appendableTo.appendTo(delegate)
    return this
  }

  fun compareTo(another: StringBuilder?): Int = delegate.compareTo(another)

  fun indexOf(str: String?): Int = delegate.indexOf(str)

  override fun chars(): IntStream = delegate.chars()

  fun codePointBefore(index: Int): Int = delegate.codePointBefore(index)

  fun insert(
    dstOffset: Int,
    s: CharSequence?,
    start: Int,
    end: Int,
  ): AppendableToAwareStringBuilder {
    delegate.insert(dstOffset, s, start, end)
    return this
  }

  fun capacity(): Int = delegate.capacity()

  fun deleteCharAt(index: Int): AppendableToAwareStringBuilder {
    delegate.deleteCharAt(index)
    return this
  }

  fun offsetByCodePoints(index: Int, codePointOffset: Int): Int =
    delegate.offsetByCodePoints(index, codePointOffset)

  fun append(
    str: CharArray,
    offset: Int,
    len: Int,
  ): AppendableToAwareStringBuilder {
    delegate.appendRange(str, offset, offset + len)
    return this
  }

  fun insert(offset: Int, f: Float): AppendableToAwareStringBuilder {
    delegate.insert(offset, f)
    return this
  }

  fun insert(offset: Int, str: CharArray): AppendableToAwareStringBuilder {
    delegate.insert(offset, str)
    return this
  }

  fun appendCodePoint(codePoint: Int): AppendableToAwareStringBuilder {
    delegate.appendCodePoint(codePoint)
    return this
  }

  fun reverse(): AppendableToAwareStringBuilder {
    delegate.reverse()
    return this
  }

  fun insert(offset: Int, str: String?): AppendableToAwareStringBuilder {
    delegate.insert(offset, str)
    return this
  }

  fun append(d: Double): AppendableToAwareStringBuilder {
    delegate.append(d)
    return this
  }

  fun append(lng: Long): AppendableToAwareStringBuilder {
    delegate.append(lng)
    return this
  }

  fun append(s: CharSequence?): AppendableToAwareStringBuilder {
    delegate.append(s)
    return this
  }

  fun setCharAt(index: Int, ch: Char) {
    delegate.setCharAt(index, ch)
  }

  fun setLength(newLength: Int) {
    delegate.setLength(newLength)
  }

  fun lastIndexOf(str: String?): Int = delegate.lastIndexOf(str)

  fun append(i: Int): AppendableToAwareStringBuilder {
    delegate.append(i)
    return this
  }

  fun insert(
    index: Int,
    str: CharArray,
    offset: Int,
    len: Int,
  ): AppendableToAwareStringBuilder {
    delegate.insert(index, str, offset, len)
    return this
  }

  fun insert(offset: Int, c: Char): AppendableToAwareStringBuilder {
    delegate.insert(offset, c)
    return this
  }

  fun append(obj: Any?): AppendableToAwareStringBuilder {
    delegate.append(obj)
    return this
  }

  fun append(c: Char): AppendableToAwareStringBuilder {
    delegate.append(c)
    return this
  }

  fun codePointAt(index: Int): Int = delegate.codePointAt(index)

  fun append(b: Boolean): AppendableToAwareStringBuilder {
    delegate.append(b)
    return this
  }

  fun substring(start: Int): String? = delegate.substring(start)

  fun replace(
    start: Int,
    end: Int,
    str: String,
  ): AppendableToAwareStringBuilder {
    delegate.replace(start, end, str)
    return this
  }

  fun insert(dstOffset: Int, s: CharSequence?): AppendableToAwareStringBuilder {
    delegate.insert(dstOffset, s)
    return this
  }

  override fun codePoints(): IntStream = delegate.codePoints()

  fun insert(offset: Int, d: Double): AppendableToAwareStringBuilder {
    delegate.insert(offset, d)
    return this
  }

  fun codePointCount(beginIndex: Int, endIndex: Int): Int =
    delegate.codePointCount(beginIndex, endIndex)

  fun append(str: CharArray): AppendableToAwareStringBuilder {
    delegate.append(str)
    return this
  }

  fun ensureCapacity(minimumCapacity: Int) {
    delegate.ensureCapacity(minimumCapacity)
  }

  fun substring(start: Int, end: Int): String? = delegate.substring(start, end)

  fun delete(start: Int, end: Int): AppendableToAwareStringBuilder {
    delegate.delete(start, end)
    return this
  }

  fun insert(offset: Int, l: Long): AppendableToAwareStringBuilder {
    delegate.insert(offset, l)
    return this
  }

  fun getChars(
    srcBegin: Int,
    srcEnd: Int,
    dst: CharArray?,
    dstBegin: Int,
  ) {
    delegate.getChars(srcBegin, srcEnd, dst, dstBegin)
  }

  fun append(f: Float): AppendableToAwareStringBuilder {
    delegate.append(f)
    return this
  }

  fun insert(offset: Int, i: Int): AppendableToAwareStringBuilder {
    delegate.insert(offset, i)
    return this
  }

  fun append(
    s: CharSequence?,
    start: Int,
    end: Int,
  ): AppendableToAwareStringBuilder {
    delegate.append(s, start, end)
    return this
  }

  fun trimToSize() {
    delegate.trimToSize()
  }

  fun insert(offset: Int, obj: Any?): AppendableToAwareStringBuilder {
    delegate.insert(offset, obj)
    return this
  }

  fun lastIndexOf(str: String, fromIndex: Int): Int = delegate.lastIndexOf(str, fromIndex)

  fun append(sb: StringBuffer?): AppendableToAwareStringBuilder {
    delegate.append(sb)
    return this
  }

  fun insert(offset: Int, b: Boolean): AppendableToAwareStringBuilder {
    delegate.insert(offset, b)
    return this
  }

  fun append(str: String?): AppendableToAwareStringBuilder {
    delegate.append(str)
    return this
  }

  fun indexOf(str: String, fromIndex: Int): Int = delegate.indexOf(str, fromIndex)
}
