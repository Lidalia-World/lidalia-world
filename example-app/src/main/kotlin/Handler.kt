package uk.org.lidalia.example

import uk.org.lidalia.uri.api.Path
import uk.org.lidalia.uri.api.PathAndQuery

interface Message<T> {
  val body: T
}

interface Request {
  val pathAndQuery: PathAndQuery
  val path: Path
    get() = pathAndQuery.path
}

interface Response {
  val status: Int

  fun isSuccess(): Boolean
}

typealias Handler = (Request) -> Response
