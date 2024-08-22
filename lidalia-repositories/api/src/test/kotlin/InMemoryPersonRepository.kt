package uk.org.lidalia.repositories.person

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.repositories.api.Entity
import uk.org.lidalia.repositories.api.Id
import uk.org.lidalia.repositories.api.Identifier
import uk.org.lidalia.repositories.api.Metadata
import uk.org.lidalia.repositories.api.MutableRepository
import uk.org.lidalia.repositories.api.UnpersistedEntity
import uk.org.lidalia.repositories.api.VersionId
import uk.org.lidalia.repositories.generic.GenericRepository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface PersonRepository : MutableRepository<
  PersonId,
  PersonIdentifier,
  Person,
  EntityMetadata,
  UnpersistedPerson,
  >

class InMemoryPersonRepository : PersonRepository {

  private val store = ConcurrentHashMap<PersonId, Person>()

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
}

class PersonRepository2 :
  PersonRepository,
  GenericRepository<PersonId, PersonIdentifier, Person, UnpersistedPerson>(
    { PersonId(UUID.randomUUID()) },
  )

sealed interface PersonIdentifier : Identifier<PersonId>

data class PersonId(
  private val uuid: UUID,
) : Id<PersonId>, PersonIdentifier {
  override val id: PersonId = this
}

data class EntityMetadata(
  val created: Instant,
  override val versionId: VersionId,
  val lastUpdated: Instant,
) : Metadata

data class UuidVersionId(
  private val uuid: UUID,
) : VersionId

data class Person(
  override val id: PersonId,
  override val metadata: EntityMetadata,
  val name: String,
) : Entity<PersonId, EntityMetadata>, PersonIdentifier

data class UnpersistedPerson(
  val name: String,
) : UnpersistedEntity<PersonId, Person, EntityMetadata> {
  override fun toEntity(id: PersonId, metadata: EntityMetadata): Person = Person(
    id = id,
    name = name,
    metadata = metadata,
  )
}
