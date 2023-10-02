abstract class InvalidEncoding protected constructor(
  val invalidEncoding: Any,
  message: String? = null,
  cause: Throwable? = null,
) : Exception(message, cause)
