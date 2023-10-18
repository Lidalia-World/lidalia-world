abstract class NotAnEncodedString protected constructor(
  invalidEncoding: String,
  message: String? = null,
  cause: Throwable? = null,
) : InvalidEncoding(
    invalidEncoding,
    message,
    cause,
  )
