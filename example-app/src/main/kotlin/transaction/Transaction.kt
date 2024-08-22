package uk.org.lidalia.example.transaction

import uk.org.lidalia.example.Handler
import uk.org.lidalia.example.Request

interface TransactionFactory {
  fun transaction(): Transaction
}

interface Transaction {
  fun commit()

  fun rollback()
}

fun Transaction.withTransaction(app: Handler): Handler = { request: Request ->
  app
    .invoke(request)
    .also { response ->
      when {
        response.isSuccess() -> commit()
        else -> rollback()
      }
    }
}
