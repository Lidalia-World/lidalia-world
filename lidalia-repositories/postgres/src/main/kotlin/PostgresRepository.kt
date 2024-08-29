package uk.org.lidalia.repositories.postgres

import uk.org.lidalia.repositories.api.Entity
import uk.org.lidalia.repositories.api.Id
import uk.org.lidalia.repositories.api.Identifier
import uk.org.lidalia.repositories.api.Metadata
import uk.org.lidalia.repositories.api.Repository

abstract class PostgresRepository<
  EntityId : Id<EntityId>,
  EntityIdentifier : Identifier<EntityId>,
  out M : Metadata,
  out E : Entity<EntityId, M>,
  > : Repository<EntityId, EntityIdentifier, M, E>
