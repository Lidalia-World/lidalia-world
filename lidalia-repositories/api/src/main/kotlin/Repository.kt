package uk.org.lidalia.repositories.api

import arrow.core.Either
import java.time.Instant

interface Repository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  out M : Metadata,
  out E : Entity<EntityId, M>,
> {

  fun get(identifier: EntityIdentifier): E?

  @Suppress("UNCHECKED_CAST")
  fun materialise(identifier: EntityIdentifier): E? = when (identifier) {
    is Entity<*, *> -> identifier as E
    else -> get(identifier)
  }
}

interface MutableRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  E : Entity<EntityId, M>,
  M : Metadata,
  P : UnpersistedEntity<EntityId, E, M>,
> : Repository<EntityId, EntityIdentifier, M, E> {
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

interface Entity<out EntityId : Id<EntityId>, out M : Metadata> : Identifier<EntityId> {
  val metadata: M
}

interface VersionId

@Suppress("unused")
interface EntityVersion<out EntityId : Id<EntityId>> {
  val entityId: EntityId
  val versionId: VersionId
  val timestamp: Instant
}

interface UnpersistedEntity<
  EntityId : Id<EntityId>,
  E : Entity<EntityId, M>,
  M : Metadata,
> {
  fun toEntity(id: EntityId, metadata: M): E
}

interface Metadata {
  val versionId: VersionId
}
