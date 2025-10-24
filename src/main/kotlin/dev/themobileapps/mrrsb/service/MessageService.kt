package dev.themobileapps.mrrsb.service

import dev.themobileapps.mrrsb.dto.response.MessageDto
import dev.themobileapps.mrrsb.entity.OutboundSms
import dev.themobileapps.mrrsb.exception.ResourceNotFoundException
import dev.themobileapps.mrrsb.exception.UnauthorizedAccessException
import dev.themobileapps.mrrsb.repository.OutboundSmsRepository
import dev.themobileapps.mrrsb.util.DateTimeUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service for managing outbound SMS messages
 */
@Service
@Transactional(readOnly = true)
class MessageService(
    private val outboundSmsRepository: OutboundSmsRepository,
    private val dateTimeUtils: DateTimeUtils
) {
    
    /**
     * Get paginated messages for a user
     * 
     * @param userId The user ID
     * @param pageable Pagination and sorting parameters
     * @param after Optional filter to get messages after a specific date
     * @return Page of messages
     */
    fun getUserMessages(
        userId: Int,
        pageable: Pageable,
        after: LocalDateTime?
    ): Page<MessageDto> {
        val messages = if (after != null) {
            // For 'after' queries, we need to convert list to page
            val messageList = outboundSmsRepository
                .findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(userId, after)
            
            // Create a sublist based on pagination
            val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(messageList.size)
            val end = (start + pageable.pageSize).coerceAtMost(messageList.size)
            val pageContent = if (start < messageList.size) messageList.subList(start, end) else emptyList()
            
            org.springframework.data.domain.PageImpl(
                pageContent,
                pageable,
                messageList.size.toLong()
            )
        } else {
            outboundSmsRepository
                .findByPersonPersonIdOrderByDateRequestedDesc(userId, pageable)
        }
        
        return messages.map { convertToDto(it) }
    }
    
    /**
     * Get a specific message by ID
     * 
     * @param messageId The message ID
     * @param userId The user ID (for authorization check)
     * @return The message
     * @throws ResourceNotFoundException if message not found
     * @throws UnauthorizedAccessException if message doesn't belong to user
     */
    fun getMessage(messageId: Int, userId: Int): MessageDto {
        val message = outboundSmsRepository.findById(messageId)
            .orElseThrow { ResourceNotFoundException("Message with ID $messageId not found") }
        
        // Check if message belongs to user
        if (message.person?.personId != userId) {
            throw UnauthorizedAccessException("You are not authorized to access this message")
        }
        
        return convertToDto(message)
    }
    
    /**
     * Get count of unread messages for a user
     * 
     * In this system, unread messages are those with dateSent = null
     * 
     * @param userId The user ID
     * @return Count of unread messages
     */
    fun getUnreadCount(userId: Int): Long {
        return outboundSmsRepository.countByPersonPersonIdAndDateSentIsNull(userId)
    }
    
    /**
     * Convert OutboundSms entity to MessageDto
     * 
     * Handles timezone adjustment for database timestamps
     * 
     * @param sms The entity to convert
     * @return The DTO
     */
    fun convertToDto(sms: OutboundSms): MessageDto {
        // Adjust for timezone (UTC+8)
        val adjustedDate = dateTimeUtils.adjustFromDatabaseTime(sms.dateRequested)
        
        return MessageDto(
            id = sms.smsId,
            message = sms.message,
            alertLevel = sms.alertLevel,
            dateRequested = adjustedDate,
            dateSent = sms.dateSent?.let { dateTimeUtils.adjustFromDatabaseTime(it) },
            status = sms.status,
            mobileNo = sms.mobileNo,
            requestedBy = sms.requestedBy
        )
    }
}
