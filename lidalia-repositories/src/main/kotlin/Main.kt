package uk.org.lidalia.repositories.main

import uk.org.lidalia.repositories.MutableRepository
import uk.org.lidalia.repositories.person.Person
import uk.org.lidalia.repositories.person.PersonId
import uk.org.lidalia.repositories.person.PersonIdentifier
import uk.org.lidalia.repositories.person.PersonRepository
import uk.org.lidalia.repositories.person.PersonRepository2
import uk.org.lidalia.repositories.person.UnpersistedPerson

fun main() {
  crud(PersonRepository())
  crud(PersonRepository2())
}

fun crud(repository: MutableRepository<PersonId, PersonIdentifier, Person, UnpersistedPerson>) {

  val person: Person = repository.create(UnpersistedPerson("Rob"))

  println("person == repository.get(person): " + (person == repository.get(person)))
  println("person == repository.get(person.id): " + (person == repository.get(person.id)))
  println("person == repository.materialise(person): " + (person == repository.materialise(person)))
  println(
    "person == repository.materialise(person.id): " + (person == repository.materialise(person.id)),
  )

  val updated = repository.put(
    person.id,
    UnpersistedPerson("Bob"),
    person.metadata.versionId,
  ).getOrNull()

  println("updated == repository.get(person): " + (updated == repository.get(person)))
  println("updated == repository.get(person.id): " + (updated == repository.get(person.id)))
  println(
    "person == repository.materialise(person): " + (updated == repository.materialise(person)),
  )
  println(
    "person == repository.materialise(person.id): " +
      (updated == repository.materialise(person.id)),
  )

  repository.delete(person)

  println("repository.get(person.id): " + repository.get(person.id))

  println()
}
