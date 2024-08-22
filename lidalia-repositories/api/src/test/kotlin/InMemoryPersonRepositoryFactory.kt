package uk.org.lidalia.repositories.api

import uk.org.lidalia.repositories.person.PersonRepository
import uk.org.lidalia.repositories.person.PersonRepository2

class InMemoryPersonRepositoryFactory {
  fun forTransaction(transaction: InMemoryTransaction): PersonRepository = PersonRepository2()
}
