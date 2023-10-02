interface Encodable<Self : Encodable<Self>> {
  fun <RawEncoded, E : Encoded<Self, RawEncoded>> encode(
    encoder: Encoder<Self, RawEncoded, E>,
  ): E {
    return encoder.encode(this as Self)
  }
}
