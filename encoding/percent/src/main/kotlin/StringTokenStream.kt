package uk.org.lidalia.encoding.percent

internal abstract class StringTokenStream<T : Any>(
  protected val input: String,
) : Iterator<T> {
  protected var index: Int = 0

  override fun hasNext(): Boolean = index < input.length
}
