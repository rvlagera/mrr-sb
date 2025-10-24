package dev.themobileapps.mrrsb.repository

import dev.themobileapps.mrrsb.entity.Person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime

/**
 * Integration tests for PersonRepository
 */
@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var personRepository: PersonRepository

    private lateinit var testPerson1: Person
    private lateinit var testPerson2: Person
    private lateinit var inactivePerson: Person

    @BeforeEach
    fun setup() {
        // Create test persons
        testPerson1 = Person(
            username = "testuser1",
            password = "password123",
            name = "Test User 1",
            mobileNo = "1234567890",
            active = true,
            dateCreated = LocalDateTime.now()
        )

        testPerson2 = Person(
            username = "testuser2",
            password = "password456",
            name = "Test User 2",
            mobileNo = "0987654321",
            active = true,
            dateCreated = LocalDateTime.now()
        )

        inactivePerson = Person(
            username = "inactiveuser",
            password = "password789",
            name = "Inactive User",
            mobileNo = "1111111111",
            active = false,
            dateCreated = LocalDateTime.now()
        )

        // Persist test data
        entityManager.persist(testPerson1)
        entityManager.persist(testPerson2)
        entityManager.persist(inactivePerson)
        entityManager.flush()
    }

    @Test
    fun `should find person by username`() {
        // When
        val found = personRepository.findByUsername("testuser1")

        // Then
        assertThat(found).isNotNull
        assertThat(found?.username).isEqualTo("testuser1")
        assertThat(found?.name).isEqualTo("Test User 1")
        assertThat(found?.mobileNo).isEqualTo("1234567890")
    }

    @Test
    fun `should return null when person not found by username`() {
        // When
        val found = personRepository.findByUsername("nonexistent")

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `should find active person by username and active status`() {
        // When
        val found = personRepository.findByUsernameAndActive("testuser1", true)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.username).isEqualTo("testuser1")
        assertThat(found?.active).isTrue()
    }

    @Test
    fun `should not find inactive person when searching for active`() {
        // When
        val found = personRepository.findByUsernameAndActive("inactiveuser", true)

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `should find inactive person when searching for inactive`() {
        // When
        val found = personRepository.findByUsernameAndActive("inactiveuser", false)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.username).isEqualTo("inactiveuser")
        assertThat(found?.active).isFalse()
    }

    @Test
    fun `should save and retrieve person successfully`() {
        // Given
        val newPerson = Person(
            username = "newuser",
            password = "newpassword",
            name = "New User",
            mobileNo = "2222222222",
            active = true,
            dateCreated = LocalDateTime.now()
        )

        // When
        val saved = personRepository.save(newPerson)
        val found = personRepository.findByUsername("newuser")

        // Then
        assertThat(saved.personId).isNotZero()
        assertThat(found).isNotNull
        assertThat(found?.username).isEqualTo("newuser")
    }

    @Test
    fun `should find all persons`() {
        // When
        val allPersons = personRepository.findAll()

        // Then
        assertThat(allPersons).hasSize(3)
        assertThat(allPersons.map { it.username })
            .containsExactlyInAnyOrder("testuser1", "testuser2", "inactiveuser")
    }

    @Test
    fun `should delete person by id`() {
        // Given
        val person = personRepository.findByUsername("testuser1")
        assertThat(person).isNotNull

        // When
        personRepository.deleteById(person!!.personId)
        val found = personRepository.findByUsername("testuser1")

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `should count all persons`() {
        // When
        val count = personRepository.count()

        // Then
        assertThat(count).isEqualTo(3)
    }
}
