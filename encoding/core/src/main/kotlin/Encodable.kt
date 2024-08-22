interface Encodable<Self : Encodable<Self>> {
  @Suppress("UNCHECKED_CAST")
  fun <RawEncoded, E : Encoded<Self, RawEncoded>> encode(
    encoder: Encoder<Self, RawEncoded, E>,
  ): E = encoder.encode(this as Self)
}
