package uk.org.lidalia.example

data class OkResponse<T>(
  val entity: T,
) : Response {
  override val status: Int = 200

  override fun isSuccess(): Boolean = true
}
