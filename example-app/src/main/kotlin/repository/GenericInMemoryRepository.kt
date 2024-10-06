package uk.org.lidalia.example.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import uk.org.lidalia.example.user.EntityMetadata
import uk.org.lidalia.example.user.UuidVersionId
import uk.org.lidalia.repositories.api.Entity
import uk.org.lidalia.repositories.api.Id
import uk.org.lidalia.repositories.api.Identifier
import uk.org.lidalia.repositories.api.MutableRepository
import uk.org.lidalia.repositories.api.UnpersistedEntity
import uk.org.lidalia.repositories.api.VersionId
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

open class GenericInMemoryRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  E : Entity<EntityId, EntityMetadata>,
  P : UnpersistedEntity<EntityId, E, EntityMetadata>,
>(
  private val idGenerator: () -> EntityId,
) : MutableRepository<EntityId, EntityIdentifier, E, EntityMetadata, P> {

  private val store = ConcurrentHashMap<EntityId, E>()

  override fun get(identifier: EntityIdentifier): E? = store[identifier.id]

  override fun create(params: P): E {
    val id = idGenerator()
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
    id: EntityId,
    entity: P,
    previousVersionId: VersionId,
  ): Either<Exception, E> {
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

  override fun delete(identifier: EntityIdentifier) {
    store.remove(identifier.id)
  }
}
