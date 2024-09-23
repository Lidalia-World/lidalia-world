package uk.org.lidalia.repositories.inmemory

import arrow.core.Either
import uk.org.lidalia.repositories.api.Person
import uk.org.lidalia.repositories.api.PersonId
import uk.org.lidalia.repositories.api.PersonIdentifier
import uk.org.lidalia.repositories.api.PersonRepository
import uk.org.lidalia.repositories.api.UnpersistedPerson
import uk.org.lidalia.repositories.api.VersionId

class InMemoryPersonRepositoryFactory {
  fun forTransaction(transaction: InMemoryTransaction): PersonRepository =
    TransactionalPersonGenericRepository(
      transaction,
    )
}

class TransactionalPersonGenericRepository(
  private val transaction: InMemoryTransaction,
) : PersonRepository {

  override fun create(params: UnpersistedPerson): Person = transaction.transactionRepository.create(
    params,
  )

  override fun put(
    id: PersonId,
    entity: UnpersistedPerson,
    previousVersionId: VersionId,
  ): Either<Exception, Person> = transaction.transactionRepository.put(
    id,
    entity,
    previousVersionId,
  )

  override fun delete(identifier: PersonIdentifier) = transaction.transactionRepository.delete(
    identifier,
  )

  override fun get(identifier: PersonIdentifier): Person? = transaction.transactionRepository.get(
    identifier,
  )
}
