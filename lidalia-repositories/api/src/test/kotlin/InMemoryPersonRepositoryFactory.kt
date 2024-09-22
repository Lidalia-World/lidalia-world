package uk.org.lidalia.repositories.api

import uk.org.lidalia.repositories.person.PersonRepository
import uk.org.lidalia.repositories.person.PersonGenericRepository

class InMemoryPersonRepositoryFactory {
  fun forTransaction(transaction: InMemoryTransaction): PersonRepository = PersonGenericRepository()
}
