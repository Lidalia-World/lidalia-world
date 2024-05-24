package uk.org.lidalia.repositories.person

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.repositories.Entity
import uk.org.lidalia.repositories.Id
import uk.org.lidalia.repositories.Identifier
import uk.org.lidalia.repositories.MutableRepository
import uk.org.lidalia.repositories.UnpersistedEntity
import uk.org.lidalia.repositories.VersionId
import uk.org.lidalia.repositories.generic.GenericRepository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PersonRepository : MutableRepository<PersonId, PersonIdentifier, Person, UnpersistedPerson> {

  private val store = ConcurrentHashMap<PersonId, Person>()

  override fun get(identifier: PersonIdentifier): Person? = store[identifier.id]

  override fun put(
    id: PersonId,
    entity: UnpersistedPerson,
    previousVersionId: VersionId,
  ): Either<Exception, Person> {
    val newVersionId = UuidVersionId(UUID.randomUUID())
    val result = store.compute(id) { _, existing ->
      if (existing == null || previousVersionId == existing.versionId) {
        val now = Instant.now()
        entity.toEntity(
          id = id,
          created = existing?.created ?: now,
          lastUpdated = now,
          versionId = newVersionId,
        )
      } else {
        existing
      }
    }

    return if (result?.versionId == newVersionId) {
      result.right()
    } else {
      Exception("entity has been altered since version $previousVersionId").left()
    }
  }

  override fun delete(identifier: PersonIdentifier) {
    store.remove(identifier.id)
  }

  override fun create(params: UnpersistedPerson): Person {
    val id = PersonId(UUID.randomUUID())
    val now = Instant.now()
    val entity = params.toEntity(
      id,
      created = now,
      lastUpdated = now,
      UuidVersionId(UUID.randomUUID()),
    )
    store[id] = entity
    return entity
  }
}

class PersonRepository2 : GenericRepository<PersonId, PersonIdentifier, Person, UnpersistedPerson>(
  { PersonId(UUID.randomUUID()) },
)

sealed interface PersonIdentifier : Identifier<PersonId>

data class PersonId(
  private val uuid: UUID,
) : Id<PersonId>, PersonIdentifier {
  override val id: PersonId = this
}

data class UuidVersionId(
  private val uuid: UUID,
) : VersionId

data class Person(
  override val id: PersonId,
  override val versionId: VersionId,
  override val created: Instant,
  override val lastUpdated: Instant,
  val name: String,
) : Entity<PersonId>, PersonIdentifier

data class UnpersistedPerson(
  val name: String,
) : UnpersistedEntity<PersonId, Person> {
  override fun toEntity(
    id: PersonId,
    created: Instant,
    lastUpdated: Instant,
    versionId: VersionId,
  ): Person = Person(
    id = id,
    versionId = versionId,
    name = name,
    created = created,
    lastUpdated = lastUpdated,
  )
}
