package dev.themobileapps.mrrsb.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themobileapps.mrrsb.entity.OutboundSms
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.repository.OutboundSmsRepository
import dev.themobileapps.mrrsb.websocket.NotificationWebSocketService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockKExtension::class)
class NotificationServiceTest {

    @MockK
    private lateinit var outboundSmsRepository: OutboundSmsRepository

    @MockK(relaxed = true)
    private lateinit var webSocketService: NotificationWebSocketService

    @InjectMockKs
    private lateinit var notificationService: NotificationService

    private val objectMapper = ObjectMapper()

    @Test
    fun `should process new message notification successfully with sms_id`() {
        // Given
        val smsId = 123
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = smsId,
            person = person,
            message = "Test message",
            alertLevel = 2,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            mobileNo = "1234567890",
            status = "PENDING",
            requestedBy = "admin"
        )

        val jsonPayload = """{"sms_id": $smsId, "person_id": 1, "alert_level": 2}"""

        every { outboundSmsRepository.findById(smsId) } returns Optional.of(message)

        // When
        notificationService.processNewMessage(jsonPayload)

        // Then
        verify(exactly = 1) { outboundSmsRepository.findById(smsId) }
    }

    @Test
    fun `should process new message notification successfully with id field`() {
        // Given
        val smsId = 456
        val person = Person(
            personId = 2,
            username = "user2",
            password = "password",
            name = "User 2"
        )
        val message = OutboundSms(
            smsId = smsId,
            person = person,
            message = "Another test message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            mobileNo = "0987654321",
            status = "PENDING",
            requestedBy = "system"
        )

        val jsonPayload = """{"id": $smsId}"""

        every { outboundSmsRepository.findById(smsId) } returns Optional.of(message)

        // When
        notificationService.processNewMessage(jsonPayload)

        // Then
        verify(exactly = 1) { outboundSmsRepository.findById(smsId) }
        verify(exactly = 1) { webSocketService.notifyNewMessage(message) }
    }

    @Test
    fun `should handle message not found gracefully`() {
        // Given
        val smsId = 999
        val jsonPayload = """{"sms_id": $smsId}"""

        every { outboundSmsRepository.findById(smsId) } returns Optional.empty()

        // When
        notificationService.processNewMessage(jsonPayload)

        // Then
        verify(exactly = 1) { outboundSmsRepository.findById(smsId) }
        // Should not throw exception
    }

    @Test
    fun `should handle invalid JSON payload gracefully`() {
        // Given
        val invalidJson = """{"invalid": "data"}"""

        // When - Should not throw exception
        notificationService.processNewMessage(invalidJson)

        // Then - Should not call repository
        verify(exactly = 0) { outboundSmsRepository.findById(any()) }
    }

    @Test
    fun `should handle malformed JSON gracefully`() {
        // Given
        val malformedJson = """not valid json at all"""

        // When - Should not throw exception
        notificationService.processNewMessage(malformedJson)

        // Then - Should not call repository
        verify(exactly = 0) { outboundSmsRepository.findById(any()) }
    }

    @Test
    fun `should handle empty payload gracefully`() {
        // Given
        val emptyJson = """{}"""

        // When - Should not throw exception
        notificationService.processNewMessage(emptyJson)

        // Then - Should not call repository
        verify(exactly = 0) { outboundSmsRepository.findById(any()) }
    }

    @Test
    fun `should process notification for high alert level message`() {
        // Given
        val smsId = 789
        val person = Person(
            personId = 3,
            username = "urgentuser",
            password = "password",
            name = "Urgent User"
        )
        val message = OutboundSms(
            smsId = smsId,
            person = person,
            message = "URGENT: Critical alert",
            alertLevel = 3,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            mobileNo = "5555555555",
            status = "PENDING",
            requestedBy = "alert_system"
        )

        val jsonPayload = """{"sms_id": $smsId, "alert_level": 3}"""

        every { outboundSmsRepository.findById(smsId) } returns Optional.of(message)

        // When
        notificationService.processNewMessage(jsonPayload)

        // Then
        verify(exactly = 1) { outboundSmsRepository.findById(smsId) }
        verify(exactly = 1) { webSocketService.notifyNewMessage(message) }
    }

    @Test
    fun `should process notification for message without person`() {
        // Given
        val smsId = 111
        val message = OutboundSms(
            smsId = smsId,
            person = null,
            message = "Orphan message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            mobileNo = "1111111111",
            status = "PENDING",
            requestedBy = "system"
        )

        val jsonPayload = """{"sms_id": $smsId}"""

        every { outboundSmsRepository.findById(smsId) } returns Optional.of(message)

        // When
        notificationService.processNewMessage(jsonPayload)

        // Then
        verify(exactly = 1) { outboundSmsRepository.findById(smsId) }
        verify(exactly = 1) { webSocketService.notifyNewMessage(message) }
    }
}
