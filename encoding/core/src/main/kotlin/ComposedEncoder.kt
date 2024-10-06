class ComposedEncoder<Decoded, MidFormat, RawEncoded>(
  private val encoder1: Encoder<Decoded, MidFormat, out Encoded<Decoded, MidFormat>>,
  private val encoder2: Encoder<MidFormat, RawEncoded, out Encoded<MidFormat, RawEncoded>>,
) : Encoder<Decoded, RawEncoded, ComposedEncoded<Decoded, RawEncoded>> {
  @Throws(InvalidEncoding::class)
  override fun of(encoded: RawEncoded): ComposedEncoded<Decoded, RawEncoded> = ComposedEncoded(
    encoder1.of(encoder2.of(encoded).decode()).decode(),
    encoded,
  )

  override fun encode(decoded: Decoded): ComposedEncoded<Decoded, RawEncoded> = ComposedEncoded(
    decoded,
    encoder2.encode(encoder1.encode(decoded).raw).raw,
  )
}
