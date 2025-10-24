package dev.themobileapps.mrrsb.websocket

import dev.themobileapps.mrrsb.dto.response.MessageDto
import dev.themobileapps.mrrsb.entity.OutboundSms
import dev.themobileapps.mrrsb.service.MessageService
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

/**
 * Service for sending WebSocket notifications about new messages
 */
@Service
class NotificationWebSocketService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val messageService: MessageService
) {

    private val logger = LoggerFactory.getLogger(NotificationWebSocketService::class.java)

    /**
     * Notify a user about a new message via WebSocket
     * 
     * @param message The outbound SMS message to notify about
     */
    fun notifyNewMessage(message: OutboundSms) {
        try {
            val dto = messageService.convertToDto(message)
            val username = message.person?.username

            if (username != null) {
                // Send to specific user's queue
                messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/messages",
                    dto
                )
                logger.info("Sent WebSocket notification to user: $username for message ID: ${message.smsId}")
            } else {
                logger.warn("Cannot send notification for message ${message.smsId} - no user associated")
            }

            // Broadcast high priority alerts to topic
            if (message.alertLevel >= 2) {
                messagingTemplate.convertAndSend(
                    "/topic/alerts",
                    dto
                )
                logger.info("Broadcasted high-priority alert (level ${message.alertLevel}) to topic for message ID: ${message.smsId}")
            }

        } catch (e: Exception) {
            logger.error("Error sending WebSocket notification for message ${message.smsId}: ${e.message}", e)
        }
    }

    /**
     * Send a heartbeat message to all connected clients
     * 
     * This can be used to keep connections alive and verify connectivity
     */
    fun sendHeartbeat() {
        try {
            val heartbeatData = mapOf(
                "type" to "heartbeat",
                "timestamp" to System.currentTimeMillis()
            )
            messagingTemplate.convertAndSend("/topic/heartbeat", heartbeatData)
            logger.debug("Sent heartbeat to /topic/heartbeat")
        } catch (e: Exception) {
            logger.error("Error sending heartbeat: ${e.message}", e)
        }
    }

    /**
     * Send an unread count update to a specific user
     * 
     * @param username The username to send the update to
     * @param count The unread message count
     */
    fun sendUnreadCountUpdate(username: String, count: Long) {
        try {
            val countData = mapOf(
                "type" to "unread_count",
                "count" to count,
                "timestamp" to System.currentTimeMillis()
            )
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/unread-count",
                countData
            )
            logger.debug("Sent unread count update to user $username: $count")
        } catch (e: Exception) {
            logger.error("Error sending unread count update to $username: ${e.message}", e)
        }
    }
}
