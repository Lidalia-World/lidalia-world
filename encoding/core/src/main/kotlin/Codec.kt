package uk.org.lidalia.encoding.core

interface Codec<
  Decoded,
  RawEncoded,
  E : Encoded<Decoded, RawEncoded>,
> :
  Encoder<
    Decoded,
    RawEncoded,
    E,
  >,
  Decoder<
    Decoded,
    RawEncoded,
    E,
  >
