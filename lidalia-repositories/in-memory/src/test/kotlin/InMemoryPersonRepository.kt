package uk.org.lidalia.repositories.inmemory

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.repositories.api.EntityMetadata
import uk.org.lidalia.repositories.api.Person
import uk.org.lidalia.repositories.api.PersonId
import uk.org.lidalia.repositories.api.PersonIdentifier
import uk.org.lidalia.repositories.api.PersonRepository
import uk.org.lidalia.repositories.api.UnpersistedPerson
import uk.org.lidalia.repositories.api.UuidVersionId
import uk.org.lidalia.repositories.api.VersionId
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class InMemoryPersonRepository(
  private val store: ConcurrentMap<PersonId, Person> = ConcurrentHashMap(),
) : PersonRepository {

  override fun get(identifier: PersonIdentifier): Person? = store[identifier.id]

  override fun create(params: UnpersistedPerson): Person {
    val id = PersonId(UUID.randomUUID())
    val now = Instant.now()
    val entity = params.toEntity(
      id = id,
      metadata = EntityMetadata(
        created = now,
        lastUpdated = now,
        versionId = UuidVersionId(UUID.randomUUID()),
      ),
    )
    store[id] = entity
    return entity
  }

  override fun put(
    id: PersonId,
    entity: UnpersistedPerson,
    previousVersionId: VersionId,
  ): Either<Exception, Person> {
    val newVersionId = UuidVersionId(UUID.randomUUID())
    val result = store.compute(id) { _, existing ->
      if (existing == null || previousVersionId == existing.metadata.versionId) {
        val now = Instant.now()
        entity.toEntity(
          id = id,
          metadata = EntityMetadata(
            created = existing?.metadata?.created ?: now,
            lastUpdated = now,
            versionId = newVersionId,
          ),
        )
      } else {
        existing
      }
    }

    return if (result?.metadata?.versionId == newVersionId) {
      result.right()
    } else {
      Exception("entity has been altered since version $previousVersionId").left()
    }
  }

  override fun delete(identifier: PersonIdentifier) {
    store.remove(identifier.id)
  }

  fun copy(): InMemoryPersonRepository = InMemoryPersonRepository(ConcurrentHashMap(store))
}

class PersonGenericRepository :
  PersonRepository,
  InMemoryGenericRepository<PersonId, PersonIdentifier, EntityMetadata, Person, UnpersistedPerson>(
    { PersonId(UUID.randomUUID()) },
    { m: EntityMetadata? ->
      val now = Instant.now()
      EntityMetadata(
        created = m?.created ?: now,
        versionId = UuidVersionId(UUID.randomUUID()),
        lastUpdated = now,
      )
    },
  )
