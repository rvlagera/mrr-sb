package dev.themobileapps.mrrsb.controller

import dev.themobileapps.mrrsb.dto.response.MessageDto
import dev.themobileapps.mrrsb.dto.response.UnreadCountDto
import dev.themobileapps.mrrsb.security.CustomUserDetails
import dev.themobileapps.mrrsb.service.MessageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * REST controller for message operations
 */
@RestController
@RequestMapping("/messages")
@Tag(name = "Messages", description = "Message management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
class MessageController(
    private val messageService: MessageService
) {
    
    /**
     * Get user messages with pagination and optional filtering
     * 
     * @param user The authenticated user
     * @param page Page number (0-based)
     * @param size Page size
     * @param after Optional filter to get messages after this date
     * @return Paginated list of messages
     */
    @GetMapping
    @Operation(summary = "Get user messages")
    fun getMessages(
        @AuthenticationPrincipal user: CustomUserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) after: LocalDateTime?
    ): ResponseEntity<Page<MessageDto>> {
        val messages = messageService.getUserMessages(
            user.userId,
            PageRequest.of(page, size, Sort.by("dateRequested").descending()),
            after
        )
        return ResponseEntity.ok(messages)
    }
    
    /**
     * Get a specific message by ID
     * 
     * @param id The message ID
     * @param user The authenticated user
     * @return The message details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID")
    fun getMessage(
        @PathVariable id: Int,
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<MessageDto> {
        val message = messageService.getMessage(id, user.userId)
        return ResponseEntity.ok(message)
    }
    
    /**
     * Get count of unread messages
     * 
     * @param user The authenticated user
     * @return Unread message count
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count")
    fun getUnreadCount(
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<UnreadCountDto> {
        val count = messageService.getUnreadCount(user.userId)
        return ResponseEntity.ok(UnreadCountDto(count))
    }
}
