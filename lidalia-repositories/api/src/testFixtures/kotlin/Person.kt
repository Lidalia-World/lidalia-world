package uk.org.lidalia.repositories.api

import java.time.Instant
import java.util.UUID

interface PersonRepository : MutableRepository<
  PersonId,
  PersonIdentifier,
  Person,
  EntityMetadata,
  UnpersistedPerson,
  >

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
