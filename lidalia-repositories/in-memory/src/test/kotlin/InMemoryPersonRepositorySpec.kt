package uk.org.lidalia.repositories.inmemory

import io.kotest.core.spec.style.StringSpec
import uk.org.lidalia.repositories.api.repositorySpec

class InMemoryPersonRepositorySpec : StringSpec({
  val personRepository = InMemoryPersonRepository()
  include(repositorySpec(personRepository))
  include(repositorySpec(PersonGenericRepository()))
})
