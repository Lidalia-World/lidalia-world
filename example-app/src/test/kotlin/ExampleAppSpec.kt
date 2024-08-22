package uk.org.lidalia.repositories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import uk.org.lidalia.example.ExampleApp
import uk.org.lidalia.example.Request
import uk.org.lidalia.example.user.UserId
import uk.org.lidalia.uri.api.PathAndQuery
import java.util.UUID

class ExampleAppSpec : StringSpec({

  "app can handle a request" {
    val exampleApp = ExampleApp()

    val userId = UserId(UUID.randomUUID())

    val request = GetUserRequest(userId)

    val response = exampleApp(request)

    response.status shouldBe 404
  }
})

class GetUserRequest(
  userId: UserId,
) : Request {
  override val pathAndQuery: PathAndQuery = PathAndQuery("/users/$userId").getOrNull()!!
}
