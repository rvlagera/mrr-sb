package dev.themobileapps.mrrsb.repository

import dev.themobileapps.mrrsb.entity.OutboundSms
import dev.themobileapps.mrrsb.entity.Person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

/**
 * Integration tests for OutboundSmsRepository
 */
@DataJpaTest
class OutboundSmsRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var outboundSmsRepository: OutboundSmsRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    private lateinit var testPerson: Person
    private lateinit var message1: OutboundSms
    private lateinit var message2: OutboundSms
    private lateinit var message3: OutboundSms
    private lateinit var unreadMessage: OutboundSms

    @BeforeEach
    fun setup() {
        // Create test person
        testPerson = Person(
            username = "testuser",
            password = "password123",
            name = "Test User",
            mobileNo = "1234567890",
            active = true,
            dateCreated = LocalDateTime.now()
        )
        entityManager.persist(testPerson)
        entityManager.flush()

        val now = LocalDateTime.now()

        // Create test messages
        message1 = OutboundSms(
            person = testPerson,
            message = "Test message 1",
            alertLevel = 1,
            dateRequested = now.minusHours(3),
            dateSent = now.minusHours(2),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )

        message2 = OutboundSms(
            person = testPerson,
            message = "Test message 2",
            alertLevel = 2,
            dateRequested = now.minusHours(2),
            dateSent = now.minusHours(1),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )

        message3 = OutboundSms(
            person = testPerson,
            message = "Test message 3",
            alertLevel = 3,
            dateRequested = now.minusHours(1),
            dateSent = now,
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )

        unreadMessage = OutboundSms(
            person = testPerson,
            message = "Unread message",
            alertLevel = 1,
            dateRequested = now,
            dateSent = null,
            mobileNo = "1234567890",
            status = "PENDING",
            requestedBy = "admin"
        )

        // Persist test data
        entityManager.persist(message1)
        entityManager.persist(message2)
        entityManager.persist(message3)
        entityManager.persist(unreadMessage)
        entityManager.flush()
    }

    @Test
    fun `should find messages by person ID ordered by date requested descending`() {
        // Given
        val pageable = PageRequest.of(0, 10, Sort.by("dateRequested").descending())

        // When
        val result = outboundSmsRepository.findByPersonPersonIdOrderByDateRequestedDesc(
            testPerson.personId,
            pageable
        )

        // Then
        assertThat(result.content).hasSize(4)
        assertThat(result.content[0].message).isEqualTo("Unread message")
        assertThat(result.content[1].message).isEqualTo("Test message 3")
        assertThat(result.content[2].message).isEqualTo("Test message 2")
        assertThat(result.content[3].message).isEqualTo("Test message 1")
    }

    @Test
    fun `should support pagination for messages`() {
        // Given
        val pageable = PageRequest.of(0, 2, Sort.by("dateRequested").descending())

        // When
        val result = outboundSmsRepository.findByPersonPersonIdOrderByDateRequestedDesc(
            testPerson.personId,
            pageable
        )

        // Then
        assertThat(result.content).hasSize(2)
        assertThat(result.totalElements).isEqualTo(4)
        assertThat(result.totalPages).isEqualTo(2)
        assertThat(result.content[0].message).isEqualTo("Unread message")
        assertThat(result.content[1].message).isEqualTo("Test message 3")
    }

    @Test
    fun `should find messages after a specific date`() {
        // Given
        val afterDate = LocalDateTime.now().minusHours(2).minusMinutes(30)

        // When
        val result = outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(
            testPerson.personId,
            afterDate
        )

        // Then
        assertThat(result).hasSize(3)
        assertThat(result[0].message).isEqualTo("Unread message")
        assertThat(result[1].message).isEqualTo("Test message 3")
        assertThat(result[2].message).isEqualTo("Test message 2")
    }

    @Test
    fun `should count unread messages`() {
        // When
        val count = outboundSmsRepository.countByPersonPersonIdAndDateSentIsNull(testPerson.personId)

        // Then
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `should return zero when no unread messages`() {
        // Given - create a new person with only read messages
        val personWithReadMessages = Person(
            username = "readuser",
            password = "password",
            name = "Read User",
            active = true
        )
        entityManager.persist(personWithReadMessages)

        val readMessage = OutboundSms(
            person = personWithReadMessages,
            message = "Read message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            dateSent = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        entityManager.persist(readMessage)
        entityManager.flush()

        // When
        val count = outboundSmsRepository.countByPersonPersonIdAndDateSentIsNull(personWithReadMessages.personId)

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `should find messages by ID`() {
        // When
        val found = outboundSmsRepository.findById(message1.smsId)

        // Then
        assertThat(found).isPresent
        assertThat(found.get().message).isEqualTo("Test message 1")
        assertThat(found.get().alertLevel).isEqualTo(1)
    }

    @Test
    fun `should save and retrieve message successfully`() {
        // Given
        val newMessage = OutboundSms(
            person = testPerson,
            message = "New test message",
            alertLevel = 2,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            mobileNo = "1234567890",
            status = "PENDING",
            requestedBy = "admin"
        )

        // When
        val saved = outboundSmsRepository.save(newMessage)
        val found = outboundSmsRepository.findById(saved.smsId)

        // Then
        assertThat(saved.smsId).isNotZero()
        assertThat(found).isPresent
        assertThat(found.get().message).isEqualTo("New test message")
    }

    @Test
    fun `should return empty page when person has no messages`() {
        // Given
        val otherPerson = Person(
            username = "otheruser",
            password = "password",
            name = "Other User",
            active = true
        )
        entityManager.persist(otherPerson)
        entityManager.flush()

        val pageable = PageRequest.of(0, 10, Sort.by("dateRequested").descending())

        // When
        val result = outboundSmsRepository.findByPersonPersonIdOrderByDateRequestedDesc(
            otherPerson.personId,
            pageable
        )

        // Then
        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0)
    }

    @Test
    fun `should return empty list when no messages after date`() {
        // Given
        val futureDate = LocalDateTime.now().plusDays(1)

        // When
        val result = outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(
            testPerson.personId,
            futureDate
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should count all messages for person`() {
        // When
        val count = outboundSmsRepository.count()

        // Then
        assertThat(count).isEqualTo(4)
    }

    @Test
    fun `should verify messages are ordered by date descending`() {
        // Given
        val pageable = PageRequest.of(0, 10)

        // When
        val result = outboundSmsRepository.findByPersonPersonIdOrderByDateRequestedDesc(
            testPerson.personId,
            pageable
        )

        // Then
        val dates = result.content.map { it.dateRequested }
        assertThat(dates).isSortedAccordingTo(Comparator.reverseOrder())
    }
}
