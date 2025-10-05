@file:OptIn(ExperimentalAtomicApi::class)

package uk.org.lidalia.repositories.inmemory

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class InMemoryTransaction(
  private val repository: AtomicReference<InMemoryPersonRepository>,
) {

  private val initialRepository = repository.load()
  var transactionRepository = initialRepository.copy()

  fun rollback() {
    transactionRepository = initialRepository.copy()
  }

  fun commit() {
    repository.compareAndSet(initialRepository, transactionRepository)
  }
}
