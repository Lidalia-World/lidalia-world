package uk.org.lidalia.repositories.inmemory

import io.kotest.mpp.atomics.AtomicReference

class InMemoryTransaction(
  private val repository: AtomicReference<InMemoryPersonRepository>,
) {

  private val initialRepository = repository.value
  var transactionRepository = initialRepository.copy()

  fun rollback() {
    transactionRepository = initialRepository.copy()
  }

  fun commit() {
    repository.compareAndSet(initialRepository, transactionRepository)
  }
}
