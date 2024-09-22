package uk.org.lidalia.repositories.inmemory

import uk.org.lidalia.repositories.api.PersonRepository

class InMemoryPersonRepositoryFactory {
  fun forTransaction(transaction: InMemoryTransaction): PersonRepository = PersonGenericRepository()
}
