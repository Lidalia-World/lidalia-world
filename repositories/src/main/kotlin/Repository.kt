package uk.org.lidalia.repositories

interface Repository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  out E : Entity<EntityId>
> {
  fun get(identifier: EntityIdentifier): E?
}

interface MutableRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  E : Entity<EntityId>,
  P : UnpersistedEntity<EntityId, E>,
> : Repository<EntityId, EntityIdentifier, E> {
  fun create(params: P): E
  fun put(entity: E): E
  fun delete(identifier: EntityIdentifier)
}

interface Identifier<out EntityId: Id<EntityId>> {
  val id: EntityId
}

interface Id<out EntityId: Id<EntityId>> : Identifier<EntityId> {
  override val id: EntityId
}

interface Entity<out EntityId: Id<EntityId>> : Identifier<EntityId>

interface UnpersistedEntity<EntityId: Id<EntityId>, E : Entity<EntityId>> {
  fun withId(id: EntityId): E
}
