package uk.org.lidalia.encoding.core

interface InvalidEncoding<Decoded, RawEncoded, E : Encoded<Decoded, RawEncoded>> {
  val invalidEncoding: RawEncoded
  val decoder: Decoder<Decoded, RawEncoded, E>
  val message: String?
  val cause: Throwable?
}
