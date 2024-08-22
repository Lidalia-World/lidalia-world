package uk.org.lidalia.lang

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.random.Random
import kotlin.text.Charsets.UTF_8

class Bytes private constructor(
  private val bytes: ByteArray,
  private val fromIndex: Int,
  private val toIndex: Int,
) : AbstractList<Byte>() {

  fun array(): ByteArray = bytes.copyOfRange(fromIndex, toIndex)

  @JvmOverloads
  fun string(charset: Charset = UTF_8): String = String(
    bytes = bytes,
    offset = fromIndex,
    length = size,
    charset = charset,
  )

  fun inputStream(): InputStream = ByteArrayInputStream(bytes, fromIndex, size)

  // TODO should this throw if length is != 4?
  fun integer(): Int = ByteBuffer.wrap(bytes).getInt(fromIndex)

  fun bigInteger(): BigInteger = BigInteger(array())

  fun unsignedBigInteger(): BigInteger = BigInteger(1, array())

  override val size: Int = toIndex - fromIndex

  override fun get(index: Int): Byte = bytes[fromIndex + index]

  fun take(number: Int): Bytes = unsafeSublist(0, minOf(number, size))

  fun drop(number: Int): Bytes = unsafeSublist(minOf(number, size), size)

  fun split(index: Int): Pair<Bytes, Bytes> = take(index) to drop(index)

  /**
   * Returns an equivalent Bytes that has no reference to the original's byte array, allowing the
   * original's byte array to be garbage collected.
   *
   * For instance, if you had a `Bytes` representing 100MB of data, and did a `take(1024)` to return
   * the first kB, the returned `Bytes` would still hold a reference to the 100MB byte array.
   * Calling `take(1024).detach()` would give you a copy of those bytes, allowing the original 100MB
   * to be garbage collected.
   *
   * @return an equivalent Bytes that has no reference to the original's byte array
   */
  fun detach(): Bytes = if (bytes.size == size) {
    this
  } else {
    unsafe(array())
  }

  override fun subList(fromIndex: Int, toIndex: Int): Bytes {
    validate(fromIndex, toIndex)
    return unsafeSublist(fromIndex, toIndex)
  }

  private fun unsafeSublist(fromIndex: Int, toIndex: Int) = when {
    fromIndex == toIndex -> empty
    fromIndex == 0 && toIndex == size -> this
    else -> {
      Bytes(
        bytes = bytes,
        fromIndex = this.fromIndex + fromIndex,
        toIndex = this.fromIndex + toIndex,
      )
    }
  }

  private fun validate(fromIndex: Int, toIndex: Int) {
    if (fromIndex < 0) {
      throw IndexOutOfBoundsException("fromIndex [$fromIndex] must be >= 0")
    }
    require(fromIndex <= toIndex) { "fromIndex [$fromIndex] must be <= to toIndex [$toIndex]" }
    val size = size
    if (toIndex > size) {
      throw IndexOutOfBoundsException("toIndex [$toIndex] must be <= to size() [$size]")
    }
  }

  companion object {

    operator fun invoke(bytes: ByteArray): Bytes = unsafe(bytes.copyOf())

    operator fun invoke(`in`: InputStream): Bytes {
      val out = ByteArrayOutputStream()
      copy(`in`, out)
      return unsafe(out.toByteArray())
    }

    private const val BUF_SIZE = 0x1000 // 4K

    private fun copy(from: InputStream, to: OutputStream) {
      val buf = ByteArray(BUF_SIZE)
      while (true) {
        val r = from.read(buf)
        if (r == -1) {
          break
        }
        to.write(buf, 0, r)
      }
    }

    @JvmOverloads
    operator fun invoke(text: String, charset: Charset = UTF_8): Bytes =
      unsafe(text.toByteArray(charset))

    operator fun invoke(bigInteger: BigInteger): Bytes = unsafe(bigInteger.toByteArray())

    operator fun invoke(b: Byte): Bytes = unsafe(byteArrayOf(b))

    operator fun invoke(integer: Int): Bytes = unsafe(
      ByteBuffer.allocate(4).putInt(integer).array(),
    )

    val empty = unsafe(ByteArray(0))

    operator fun invoke(vararg elements: Bytes): Bytes = invoke(elements.asList())

    // TODO this could be more efficient with no copying by storing the List<Bytes> as the
    //  state of the Bytes object and doing the maths to haul data out of them as needed
    operator fun invoke(elements: Iterable<Bytes>): Bytes {
      val length = elements.sumOf { obj: Bytes -> obj.size }
      val bytes = ByteArray(length)
      var offset = 0
      for (element in elements) {
        System.arraycopy(
          element.array(),
          0,
          bytes,
          offset,
          element.size,
        )
        offset += element.size
      }
      return unsafe(bytes)
    }

    /**
     * Unsafe constructor for [Bytes].
     * No defensive copy of the byte array is taken, so the caller is
     * responsible for ensuring the byte array does not escape and so cannot be
     * mutated.
     *
     * @param bytes the raw contents
     * @return an immutable wrapper around the contents
     */
    private fun unsafe(bytes: ByteArray): Bytes = Bytes(bytes, 0, bytes.size)

    @JvmOverloads
    fun random(random: Random = Random.Default, length: Int = random.nextInt(1024)): Bytes =
      unsafe(random.nextBytes(length))
  }
}
