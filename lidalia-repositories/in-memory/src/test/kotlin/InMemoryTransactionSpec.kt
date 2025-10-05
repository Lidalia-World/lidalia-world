@file:OptIn(ExperimentalAtomicApi::class)

package uk.org.lidalia.repositories.inmemory

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import uk.org.lidalia.repositories.api.UnpersistedPerson
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class InMemoryTransactionSpec : StringSpec({

  "a transaction can be rolled back" {

    val repository = AtomicReference(InMemoryPersonRepository())

    val repositoryFactory = InMemoryPersonRepositoryFactory()
    val transaction = InMemoryTransaction(repository)

    val repository1 = repositoryFactory.forTransaction(transaction)

    val created = repository1.create(UnpersistedPerson("rob"))

    repository1.get(created.id) shouldBe created

    transaction.rollback()

    repository1.get(created.id) shouldBe null
  }

  "a transaction can be committed" {

    val repository = AtomicReference(InMemoryPersonRepository())

    val repositoryFactory = InMemoryPersonRepositoryFactory()
    val transaction = InMemoryTransaction(repository)

    val repository1 = repositoryFactory.forTransaction(transaction)

    val created = repository1.create(UnpersistedPerson("rob"))

    repository1.get(created.id) shouldBe created

    transaction.commit()

    repository1.get(created.id) shouldBe created
  }
})
