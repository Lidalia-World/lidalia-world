package uk.org.lidalia.repositories

import arrow.core.getOrElse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import uk.org.lidalia.repositories.person.InMemoryPersonRepository
import uk.org.lidalia.repositories.person.PersonRepository2
import uk.org.lidalia.repositories.person.UnpersistedPerson

class RepositorySpec : StringSpec({

  listOf(
    InMemoryPersonRepository(),
    PersonRepository2(),
  ).forEach { repository ->
    "can do a round trip for ${repository::class.simpleName}" {

      val person = repository.create(UnpersistedPerson("Rob"))

      person shouldBe repository.get(person)
      person shouldBe repository.get(person.id)
      person shouldBe repository.materialise(person)
      person shouldBe repository.materialise(person.id)

      val updated = repository.put(
        person.id,
        UnpersistedPerson("Bob"),
        person.metadata.versionId,
      ).getOrElse { throw it }

      updated.id shouldBe person.id

      updated shouldBe repository.get(person)
      updated shouldBe repository.get(updated)
      updated shouldBe repository.get(person.id)
      updated shouldBe repository.materialise(person.id)

      updated shouldBe repository.materialise(updated)
      updated shouldBe repository.materialise(updated.id)

      updated shouldNotBe repository.materialise(person)

      repository.delete(person)

      repository.get(person.id) shouldBe null
    }
  }
})
