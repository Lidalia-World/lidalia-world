package uk.org.lidalia.repositories.inmemory

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import uk.org.lidalia.repositories.api.UnpersistedPerson

class InMemoryTransactionSpec : StringSpec({

  "a transaction can be rolled back" {

    val repositoryFactory = InMemoryPersonRepositoryFactory()
    val transaction = InMemoryTransaction()

    val repository1 = repositoryFactory.forTransaction(transaction)

    val created = repository1.create(UnpersistedPerson("rob"))

    repository1.get(created.id) shouldBe created

    transaction.rollback()

    val repository2 = repositoryFactory.forTransaction(transaction)

    repository2.get(created.id) shouldBe null
  }

  "a transaction can be committed" {

    val repositoryFactory = InMemoryPersonRepositoryFactory()
    val transaction = InMemoryTransaction()

    val repository1 = repositoryFactory.forTransaction(transaction)

    val created = repository1.create(UnpersistedPerson("rob"))

    repository1.get(created.id) shouldBe created

    transaction.commit()

    val repository2 = repositoryFactory.forTransaction(transaction)

    repository2.get(created.id) shouldBe created
  }
})
