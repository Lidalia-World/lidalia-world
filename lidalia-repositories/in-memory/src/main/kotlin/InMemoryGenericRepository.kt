package uk.org.lidalia.repositories.inmemory

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
import java.util.concurrent.ConcurrentHashMap

open class InMemoryGenericRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  EntityMetadata : Metadata,
  E : Entity<EntityId, EntityMetadata>,
  P : UnpersistedEntity<EntityId, E, EntityMetadata>,
>(
  private val idGenerator: () -> EntityId,
  private val metadataGenerator: (EntityMetadata?) -> EntityMetadata,
) : MutableRepository<EntityId, EntityIdentifier, E, EntityMetadata, P> {

  private val store = ConcurrentHashMap<EntityId, E>()

  override fun get(identifier: EntityIdentifier): E? = store[identifier.id]

  override fun create(params: P): E {
    val id = idGenerator()
    val entity = params.toEntity(
      id = id,
      metadata = metadataGenerator(null),
    )
    store[id] = entity
    return entity
  }

  override fun put(
    id: EntityId,
    entity: P,
    previousVersionId: VersionId,
  ): Either<Exception, E> {
    try {
      val computed: E = store.compute(id) { _, existing ->
        if (existing == null) {
          entity.toEntity(
            id = id,
            metadata = metadataGenerator(null),
          )
        } else if (previousVersionId == existing.metadata.versionId) {
          entity.toEntity(
            id = id,
            metadata = metadataGenerator(existing.metadata),
          )
        } else {
          throw ChangedByAnotherTransactionException(
            id,
            previousVersionId,
            existing.metadata.versionId,
          )
        }
      }!!
      return computed.right()
    } catch (e: ChangedByAnotherTransactionException) {
      return e.left()
    }
  }

  override fun delete(identifier: EntityIdentifier) {
    store.remove(identifier.id)
  }
}

data class ChangedByAnotherTransactionException(
  val entityId: Id<*>,
  val expectedVersionId: VersionId,
  val actualVersionId: VersionId,
) : Exception(
    "entity id=$entityId was expected to be at expectedVersion=$expectedVersionId but was at " +
      "actualVersion=$actualVersionId",
  )
