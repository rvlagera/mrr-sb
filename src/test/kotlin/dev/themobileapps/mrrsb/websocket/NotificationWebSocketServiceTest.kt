package dev.themobileapps.mrrsb.websocket

import dev.themobileapps.mrrsb.dto.response.MessageDto
import dev.themobileapps.mrrsb.entity.OutboundSms
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.service.MessageService
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class NotificationWebSocketServiceTest {

    private lateinit var messagingTemplate: SimpMessagingTemplate
    private lateinit var messageService: MessageService
    private lateinit var notificationWebSocketService: NotificationWebSocketService

    @BeforeEach
    fun setup() {
        messagingTemplate = mockk(relaxed = true)
        messageService = mockk()
        notificationWebSocketService = NotificationWebSocketService(messagingTemplate, messageService)
    }

    @Test
    fun `should send notification to specific user`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = 1,
            person = person,
            message = "Test message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        val dto = MessageDto(
            id = 1,
            message = "Test message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            status = "SENT",
            mobileNo = "1234567890",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } returns dto

        // When
        notificationWebSocketService.notifyNewMessage(message)

        // Then
        verify { 
            messagingTemplate.convertAndSendToUser(
                "testuser",
                "/queue/messages",
                dto
            )
        }
    }

    @Test
    fun `should broadcast high priority alert to topic`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = 1,
            person = person,
            message = "Critical alert",
            alertLevel = 2,  // High priority
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        val dto = MessageDto(
            id = 1,
            message = "Critical alert",
            alertLevel = 2,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            status = "SENT",
            mobileNo = "1234567890",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } returns dto

        // When
        notificationWebSocketService.notifyNewMessage(message)

        // Then
        verify { 
            messagingTemplate.convertAndSend(
                "/topic/alerts",
                dto
            )
        }
    }

    @Test
    fun `should not broadcast low priority alert to topic`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = 1,
            person = person,
            message = "Low priority message",
            alertLevel = 0,  // Low priority
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        val dto = MessageDto(
            id = 1,
            message = "Low priority message",
            alertLevel = 0,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            status = "SENT",
            mobileNo = "1234567890",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } returns dto

        // When
        notificationWebSocketService.notifyNewMessage(message)

        // Then
        verify(exactly = 0) { 
            messagingTemplate.convertAndSend(eq("/topic/alerts"), any<Any>())
        }
    }

    @Test
    fun `should handle message without person gracefully`() {
        // Given
        val message = OutboundSms(
            smsId = 1,
            person = null,  // No person
            message = "Test message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        val dto = MessageDto(
            id = 1,
            message = "Test message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            status = "SENT",
            mobileNo = "1234567890",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } returns dto

        // When
        notificationWebSocketService.notifyNewMessage(message)

        // Then - should not send to user (no username), but should not crash
        verify(exactly = 0) { 
            messagingTemplate.convertAndSendToUser(any(), any(), any())
        }
    }

    @Test
    fun `should send heartbeat to topic`() {
        // When
        notificationWebSocketService.sendHeartbeat()

        // Then
        verify { 
            messagingTemplate.convertAndSend(
                eq("/topic/heartbeat"),
                match<Map<String, Any>> { data ->
                    data is Map<*, *> &&
                    (data["type"] as? String) == "heartbeat" &&
                    data["timestamp"] != null
                }
            )
        }
    }

    @Test
    fun `should send unread count update to user`() {
        // Given
        val username = "testuser"
        val count = 5L

        // When
        notificationWebSocketService.sendUnreadCountUpdate(username, count)

        // Then
        verify { 
            messagingTemplate.convertAndSendToUser(
                eq(username),
                eq("/queue/unread-count"),
                match<Map<String, Any>> { data ->
                    data is Map<*, *> &&
                    (data["type"] as? String) == "unread_count" &&
                    (data["count"] as? Long) == count &&
                    data["timestamp"] != null
                }
            )
        }
    }

    @Test
    fun `should handle errors when sending notification`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = 1,
            person = person,
            message = "Test message",
            alertLevel = 1,
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } throws RuntimeException("Test error")

        // When - should not throw exception
        assertDoesNotThrow {
            notificationWebSocketService.notifyNewMessage(message)
        }
    }

    @Test
    fun `should send notification for alert level exactly 2`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = 1,
            person = person,
            message = "Alert level 2",
            alertLevel = 2,  // Exactly 2
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        val dto = MessageDto(
            id = 1,
            message = "Alert level 2",
            alertLevel = 2,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            status = "SENT",
            mobileNo = "1234567890",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } returns dto

        // When
        notificationWebSocketService.notifyNewMessage(message)

        // Then - should broadcast to topic
        verify { 
            messagingTemplate.convertAndSend("/topic/alerts", dto)
        }
    }

    @Test
    fun `should send notification for alert level greater than 2`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )
        val message = OutboundSms(
            smsId = 1,
            person = person,
            message = "Critical alert",
            alertLevel = 3,  // Greater than 2
            dateRequested = LocalDateTime.now(),
            mobileNo = "1234567890",
            status = "SENT",
            requestedBy = "admin"
        )
        val dto = MessageDto(
            id = 1,
            message = "Critical alert",
            alertLevel = 3,
            dateRequested = LocalDateTime.now(),
            dateSent = null,
            status = "SENT",
            mobileNo = "1234567890",
            requestedBy = "admin"
        )

        every { messageService.convertToDto(message) } returns dto

        // When
        notificationWebSocketService.notifyNewMessage(message)

        // Then - should broadcast to topic
        verify { 
            messagingTemplate.convertAndSend("/topic/alerts", dto)
        }
    }
}
