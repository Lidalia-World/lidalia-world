package uk.org.lidalia.repositories.person

import uk.org.lidalia.repositories.Entity
import uk.org.lidalia.repositories.Identifier
import uk.org.lidalia.repositories.Id
import uk.org.lidalia.repositories.MutableRepository
import uk.org.lidalia.repositories.UnpersistedEntity
import uk.org.lidalia.repositories.generic.GenericRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PersonRepository : MutableRepository<PersonId, PersonIdentifier, Person, UnpersistedPerson> {

  private val store = ConcurrentHashMap<PersonId, Person>()

  override fun get(identifier: PersonIdentifier): Person? = when (identifier) {
    is Person -> identifier
    is PersonId -> store[identifier]
  }

  override fun put(entity: Person): Person {
    store[entity.id] = entity
    return entity
  }

  override fun delete(identifier: PersonIdentifier) {
    store.remove(identifier.id)
  }

  override fun create(params: UnpersistedPerson): Person {
    val id = PersonId(UUID.randomUUID())
    return put(params.withId(id))
  }
}

class PersonRepository2 : GenericRepository<PersonId, PersonIdentifier, Person, UnpersistedPerson>(
  { PersonId(UUID.randomUUID()) },
)

sealed interface PersonIdentifier : Identifier<PersonId>

data class PersonId(
  private val uuid: UUID
) : Id<PersonId>, PersonIdentifier {
  override val id: PersonId = this
}

data class Person(
  override val id: PersonId,
  val name: String,
) : Entity<PersonId>, PersonIdentifier

data class UnpersistedPerson(
  val name: String
) : UnpersistedEntity<PersonId, Person> {
  override fun withId(id: PersonId): Person = Person(id = id, name = name)
}
