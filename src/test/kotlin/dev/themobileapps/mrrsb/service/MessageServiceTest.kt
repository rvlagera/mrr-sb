package dev.themobileapps.mrrsb.service

import dev.themobileapps.mrrsb.entity.OutboundSms
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.exception.ResourceNotFoundException
import dev.themobileapps.mrrsb.exception.UnauthorizedAccessException
import dev.themobileapps.mrrsb.repository.OutboundSmsRepository
import dev.themobileapps.mrrsb.util.DateTimeUtils
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.util.*

/**
 * Tests for MessageService
 */
@ExtendWith(MockKExtension::class)
class MessageServiceTest {
    
    @MockK
    private lateinit var outboundSmsRepository: OutboundSmsRepository
    
    @MockK
    private lateinit var dateTimeUtils: DateTimeUtils
    
    @InjectMockKs
    private lateinit var messageService: MessageService
    
    private fun createTestPerson(id: Int = 1): Person {
        return Person(
            personId = id,
            username = "testuser",
            password = "password",
            name = "Test User",
            active = true
        )
    }
    
    private fun createTestMessage(
        id: Int = 1,
        person: Person? = createTestPerson(),
        message: String = "Test message",
        dateRequested: LocalDateTime = LocalDateTime.now(),
        dateSent: LocalDateTime? = null
    ): OutboundSms {
        return OutboundSms(
            smsId = id,
            person = person,
            message = message,
            alertLevel = 1,
            dateRequested = dateRequested,
            dateSent = dateSent,
            mobileNo = "+1234567890",
            status = "PENDING",
            requestedBy = "system"
        )
    }
    
    @Test
    fun `should get user messages with pagination`() {
        // Given
        val userId = 1
        val pageable = PageRequest.of(0, 20, Sort.by("dateRequested").descending())
        val messages = listOf(
            createTestMessage(1),
            createTestMessage(2)
        )
        val page = PageImpl(messages, pageable, 2)
        
        every { outboundSmsRepository.findByPersonPersonIdOrderByDateRequestedDesc(userId, pageable) } returns page
        every { dateTimeUtils.adjustFromDatabaseTime(any()) } answers { firstArg() }
        
        // When
        val result = messageService.getUserMessages(userId, pageable, null)
        
        // Then
        assertThat(result.content).hasSize(2)
        assertThat(result.totalElements).isEqualTo(2)
        verify { outboundSmsRepository.findByPersonPersonIdOrderByDateRequestedDesc(userId, pageable) }
    }
    
    @Test
    fun `should get user messages after a specific date`() {
        // Given
        val userId = 1
        val after = LocalDateTime.now().minusDays(1)
        val pageable = PageRequest.of(0, 20, Sort.by("dateRequested").descending())
        val messages = listOf(createTestMessage(1))
        
        every { 
            outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(userId, after)
        } returns messages
        every { dateTimeUtils.adjustFromDatabaseTime(any()) } answers { firstArg() }
        
        // When
        val result = messageService.getUserMessages(userId, pageable, after)
        
        // Then
        assertThat(result.content).hasSize(1)
        verify { 
            outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(userId, after)
        }
    }
    
    @Test
    fun `should get empty page when no messages after date`() {
        // Given
        val userId = 1
        val after = LocalDateTime.now()
        val pageable = PageRequest.of(0, 20, Sort.by("dateRequested").descending())
        
        every { 
            outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(userId, after)
        } returns emptyList()
        
        // When
        val result = messageService.getUserMessages(userId, pageable, after)
        
        // Then
        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(0)
    }
    
    @Test
    fun `should get message by ID`() {
        // Given
        val messageId = 1
        val userId = 1
        val message = createTestMessage(messageId)
        
        every { outboundSmsRepository.findById(messageId) } returns Optional.of(message)
        every { dateTimeUtils.adjustFromDatabaseTime(any()) } answers { firstArg() }
        
        // When
        val result = messageService.getMessage(messageId, userId)
        
        // Then
        assertThat(result.id).isEqualTo(messageId)
        assertThat(result.message).isEqualTo("Test message")
        verify { outboundSmsRepository.findById(messageId) }
    }
    
    @Test
    fun `should throw ResourceNotFoundException when message not found`() {
        // Given
        val messageId = 999
        val userId = 1
        
        every { outboundSmsRepository.findById(messageId) } returns Optional.empty()
        
        // When & Then
        assertThatThrownBy { messageService.getMessage(messageId, userId) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("Message with ID $messageId not found")
    }
    
    @Test
    fun `should throw UnauthorizedAccessException when message belongs to different user`() {
        // Given
        val messageId = 1
        val userId = 2
        val message = createTestMessage(messageId, createTestPerson(1))
        
        every { outboundSmsRepository.findById(messageId) } returns Optional.of(message)
        
        // When & Then
        assertThatThrownBy { messageService.getMessage(messageId, userId) }
            .isInstanceOf(UnauthorizedAccessException::class.java)
            .hasMessageContaining("You are not authorized to access this message")
    }
    
    @Test
    fun `should get unread count`() {
        // Given
        val userId = 1
        val count = 5L
        
        every { outboundSmsRepository.countByPersonPersonIdAndDateSentIsNull(userId) } returns count
        
        // When
        val result = messageService.getUnreadCount(userId)
        
        // Then
        assertThat(result).isEqualTo(count)
        verify { outboundSmsRepository.countByPersonPersonIdAndDateSentIsNull(userId) }
    }
    
    @Test
    fun `should convert entity to DTO with timezone adjustment`() {
        // Given
        val dateRequested = LocalDateTime.of(2024, 10, 24, 12, 0, 0)
        val dateSent = LocalDateTime.of(2024, 10, 24, 12, 30, 0)
        val message = createTestMessage(
            id = 1,
            dateRequested = dateRequested,
            dateSent = dateSent
        )
        
        val adjustedRequested = LocalDateTime.of(2024, 10, 24, 4, 0, 0)
        val adjustedSent = LocalDateTime.of(2024, 10, 24, 4, 30, 0)
        
        every { dateTimeUtils.adjustFromDatabaseTime(dateRequested) } returns adjustedRequested
        every { dateTimeUtils.adjustFromDatabaseTime(dateSent) } returns adjustedSent
        
        // When
        val result = messageService.convertToDto(message)
        
        // Then
        assertThat(result.id).isEqualTo(1)
        assertThat(result.message).isEqualTo("Test message")
        assertThat(result.dateRequested).isEqualTo(adjustedRequested)
        assertThat(result.dateSent).isEqualTo(adjustedSent)
        assertThat(result.alertLevel).isEqualTo(1)
        assertThat(result.status).isEqualTo("PENDING")
        assertThat(result.mobileNo).isEqualTo("+1234567890")
        assertThat(result.requestedBy).isEqualTo("system")
    }
    
    @Test
    fun `should convert entity to DTO with null dateSent`() {
        // Given
        val dateRequested = LocalDateTime.of(2024, 10, 24, 12, 0, 0)
        val message = createTestMessage(
            id = 1,
            dateRequested = dateRequested,
            dateSent = null
        )
        
        val adjustedRequested = LocalDateTime.of(2024, 10, 24, 4, 0, 0)
        
        every { dateTimeUtils.adjustFromDatabaseTime(dateRequested) } returns adjustedRequested
        
        // When
        val result = messageService.convertToDto(message)
        
        // Then
        assertThat(result.dateSent).isNull()
    }
    
    @Test
    fun `should handle pagination for after query with second page`() {
        // Given
        val userId = 1
        val after = LocalDateTime.now().minusDays(1)
        val pageable = PageRequest.of(1, 10, Sort.by("dateRequested").descending())
        val messages = (1..25).map { createTestMessage(it) }
        
        every { 
            outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(userId, after)
        } returns messages
        every { dateTimeUtils.adjustFromDatabaseTime(any()) } answers { firstArg() }
        
        // When
        val result = messageService.getUserMessages(userId, pageable, after)
        
        // Then
        assertThat(result.content).hasSize(10)
        assertThat(result.totalElements).isEqualTo(25)
        assertThat(result.number).isEqualTo(1)
    }
    
    @Test
    fun `should handle pagination for after query with out of bounds page`() {
        // Given
        val userId = 1
        val after = LocalDateTime.now().minusDays(1)
        val pageable = PageRequest.of(5, 10, Sort.by("dateRequested").descending())
        val messages = listOf(createTestMessage(1), createTestMessage(2))
        
        every { 
            outboundSmsRepository.findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(userId, after)
        } returns messages
        
        // When
        val result = messageService.getUserMessages(userId, pageable, after)
        
        // Then
        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isEqualTo(2)
    }
}
