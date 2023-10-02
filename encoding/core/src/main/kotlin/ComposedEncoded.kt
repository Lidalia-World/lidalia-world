class ComposedEncoded<Decoded, RawEncoded> internal constructor(
  decoded: Decoded,
  rawEncoded: RawEncoded,
) : CachedEncodedBase<Decoded, RawEncoded>(rawEncoded, decoded)
