package uk.org.lidalia.example

import uk.org.lidalia.example.transaction.InMemoryTransactionFactory
import uk.org.lidalia.example.transaction.Transaction
import uk.org.lidalia.example.transaction.TransactionFactory
import uk.org.lidalia.example.transaction.withTransaction
import uk.org.lidalia.example.user.GetUserHandler
import uk.org.lidalia.example.user.InMemoryUserRepository
import uk.org.lidalia.example.user.NotFoundResponse

class PerRequestAppFactory(
  private val transactionFactory: TransactionFactory = InMemoryTransactionFactory(),
) : (Request) -> Handler {
  override fun invoke(request: Request): Handler {
    val transaction = transactionFactory.transaction()
    val app: Handler = buildApp(request, transaction)
    return transaction.withTransaction(app)
  }

  private fun buildApp(request: Request, transaction: Transaction): Handler {
    val userRepository = InMemoryUserRepository()
    return RoutesHandler { r ->
      val h = GetUserHandler(userRepository)
      val getUserRequest = h.matches(r)
      if (getUserRequest != null) {
        h
      } else {
        { _ -> NotFoundResponse() }
      }
    }
  }
}

interface RequestMatching<T : Request> {
  fun matches(request: Request): T?
}

interface RequestMatchingHandler<T : Request> : RequestMatching<T>, Handler

class RoutesHandler(
  private val handler: (Request) -> Handler,
) : Handler {
  override fun invoke(req: Request): Response = TODO("Not yet implemented")
}
