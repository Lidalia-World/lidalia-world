package uk.org.lidalia.example.user

import uk.org.lidalia.example.Response

class NotFoundResponse : Response {

  override val status: Int = 404

  override fun isSuccess(): Boolean = false
}
