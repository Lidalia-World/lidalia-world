package uk.org.lidalia.example.user

import uk.org.lidalia.example.repository.GenericInMemoryRepository
import uk.org.lidalia.repositories.api.Entity
import uk.org.lidalia.repositories.api.Id
import uk.org.lidalia.repositories.api.Identifier
import uk.org.lidalia.repositories.api.Metadata
import uk.org.lidalia.repositories.api.MutableRepository
import uk.org.lidalia.repositories.api.UnpersistedEntity
import uk.org.lidalia.repositories.api.VersionId
import java.time.Instant
import java.util.UUID

interface UserRepository : MutableRepository<
  UserId,
  UserIdentifier,
  User,
  EntityMetadata,
  UnpersistedUser,
  >

class InMemoryUserRepository :
  UserRepository,
  GenericInMemoryRepository<UserId, UserIdentifier, User, UnpersistedUser>(
    { UserId(UUID.randomUUID()) },
  )

sealed interface UserIdentifier : Identifier<UserId>

data class UserId(
  private val uuid: UUID,
) : Id<UserId>, UserIdentifier {

  override val id: UserId = this

  override fun toString(): String = uuid.toString()
}

data class EntityMetadata(
  val created: Instant,
  override val versionId: VersionId,
  val lastUpdated: Instant,
) : Metadata

data class UuidVersionId(
  private val uuid: UUID,
) : VersionId

data class User(
  override val id: UserId,
  override val metadata: EntityMetadata,
  val name: String,
) : Entity<UserId, EntityMetadata>, UserIdentifier

data class UnpersistedUser(
  val name: String,
) : UnpersistedEntity<UserId, User, EntityMetadata> {
  override fun toEntity(id: UserId, metadata: EntityMetadata): User = User(
    id = id,
    name = name,
    metadata = metadata,
  )
}
