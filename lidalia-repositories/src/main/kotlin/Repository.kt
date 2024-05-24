package uk.org.lidalia.repositories

import arrow.core.Either
import java.time.Instant

interface Repository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  out E : Entity<EntityId>,
  > {

  fun get(identifier: EntityIdentifier): E?

  @Suppress("UNCHECKED_CAST")
  fun materialise(identifier: EntityIdentifier): E? = when (identifier) {
    is Entity<*> -> identifier as E
    else -> get(identifier)
  }
}

interface MutableRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  E : Entity<EntityId>,
  P : UnpersistedEntity<EntityId, E>,
  > : Repository<EntityId, EntityIdentifier, E> {
  fun create(params: P): E

  fun put(
    id: EntityId,
    entity: P,
    previousVersionId: VersionId,
  ): Either<Exception, E>

  fun delete(identifier: EntityIdentifier)
}

interface Identifier<out EntityId : Id<EntityId>> {
  val id: EntityId
}

interface Id<out EntityId : Id<EntityId>> : Identifier<EntityId> {
  override val id: EntityId
}

interface Entity<out EntityId : Id<EntityId>> : Identifier<EntityId> {
  val metadata: Metadata
}

interface VersionId

interface EntityVersion<out EntityId : Id<EntityId>> {
  val entityId: EntityId
  val versionId: VersionId
  val timestamp: Instant
}

interface UnpersistedEntity<EntityId : Id<EntityId>, E : Entity<EntityId>> {
  fun toEntity(id: EntityId, metadata: Metadata): E
}

interface Metadata {
  val created: Instant
  val versionId: VersionId
  val lastUpdated: Instant
}
