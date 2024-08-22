package uk.org.lidalia.example.transaction

class InMemoryTransactionFactory : TransactionFactory {
  override fun transaction(): Transaction = InMemoryTransaction()
}

class InMemoryTransaction : Transaction {
  override fun commit() {}

  override fun rollback() {}
}
