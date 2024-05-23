package uk.org.lidalia.repositories.generic

import uk.org.lidalia.repositories.Entity
import uk.org.lidalia.repositories.Identifier
import uk.org.lidalia.repositories.Id
import uk.org.lidalia.repositories.MutableRepository
import uk.org.lidalia.repositories.UnpersistedEntity
import java.util.concurrent.ConcurrentHashMap

open class GenericRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  E : Entity<EntityId>,
  P : UnpersistedEntity<EntityId, E>,
>(
  private val idGenerator: () -> EntityId
) : MutableRepository<EntityId, EntityIdentifier, E, P> {
  private val store = ConcurrentHashMap<EntityId, E>()

  override fun get(identifier: EntityIdentifier): E? = store[identifier.id]

  override fun create(params: P): E {
    val id = idGenerator()
    val entity = params.withId(id)
    return put(entity)
  }

  override fun put(entity: E): E {
    store[entity.id] = entity
    return entity
  }

  override fun delete(identifier: EntityIdentifier) {
    store.remove(identifier.id)
  }
}
