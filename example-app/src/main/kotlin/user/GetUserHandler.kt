package uk.org.lidalia.example.user

import arrow.core.getOrElse
import uk.org.lidalia.example.OkResponse
import uk.org.lidalia.example.Request
import uk.org.lidalia.example.RequestMatchingHandler
import uk.org.lidalia.example.Response
import uk.org.lidalia.example.uritemplate.matches
import uk.org.lidalia.example.uritemplate.toUriTemplate
import uk.org.lidalia.uri.api.PathAndQuery
import uk.org.lidalia.uri.api.Segment
import uk.org.lidalia.uri.api.UriReference
import java.util.UUID

private val uriTemplate = "/users/{userId}".toUriTemplate().getOrElse { throw it }

class GetUserHandler(
  private val userRepository: UserRepository,
) : RequestMatchingHandler<GetUserRequest> {

  override fun matches(request: Request): GetUserRequest? = when {
    request.path.matches(uriTemplate) -> GetUserRequest(request.pathAndQuery, request)
    else -> null
  }

  override fun invoke(request: Request): Response {
    val personId = request.pathAndQuery.toPersonUri().userId
    return when (val person = userRepository.get(personId)) {
      null -> NotFoundResponse()
      else -> OkResponse(person)
    }
  }
}

data class GetUserRequest(
  val uri: UriReference,
  private val delegate: Request,
) : Request by delegate

data class PersonUri(
  val uri: UriReference,
  val userId: UserId,
)

fun PathAndQuery.toPersonUri() = PersonUri(this, this.path.segments[2].toPersonId())

fun Segment.toPersonId() = UserId(UUID.fromString(this.toString()))
