package uk.org.lidalia.repositories.generic

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.repositories.Entity
import uk.org.lidalia.repositories.Id
import uk.org.lidalia.repositories.Identifier
import uk.org.lidalia.repositories.MutableRepository
import uk.org.lidalia.repositories.UnpersistedEntity
import uk.org.lidalia.repositories.VersionId
import uk.org.lidalia.repositories.person.UuidVersionId
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

open class GenericRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  E : Entity<EntityId>,
  P : UnpersistedEntity<EntityId, E>,
  >(
  private val idGenerator: () -> EntityId,
) : MutableRepository<EntityId, EntityIdentifier, E, P> {
  private val store = ConcurrentHashMap<EntityId, E>()

  override fun get(identifier: EntityIdentifier): E? = store[identifier.id]

  override fun create(params: P): E {
    val id = idGenerator()
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

  override fun put(
    id: EntityId,
    entity: P,
    previousVersionId: VersionId,
  ): Either<Exception, E> {
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

  override fun delete(identifier: EntityIdentifier) {
    store.remove(identifier.id)
  }
}
