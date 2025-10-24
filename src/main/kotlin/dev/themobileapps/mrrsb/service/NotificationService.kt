package dev.themobileapps.mrrsb.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themobileapps.mrrsb.repository.OutboundSmsRepository
import dev.themobileapps.mrrsb.websocket.NotificationWebSocketService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service for processing database notifications for new outbound SMS messages
 */
@Service
class NotificationService(
    private val outboundSmsRepository: OutboundSmsRepository,
    private val objectMapper: ObjectMapper,
    private val webSocketService: NotificationWebSocketService
) {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    /**
     * Process a new message notification from the database
     * @param jsonPayload JSON payload containing the SMS ID and other details
     */
    fun processNewMessage(jsonPayload: String) {
        try {
            logger.debug("Processing new message notification: $jsonPayload")

            // Parse the notification payload
            val notification = objectMapper.readTree(jsonPayload)
            val smsId = notification.get("sms_id")?.asInt()
                ?: notification.get("id")?.asInt()
                ?: throw IllegalArgumentException("Missing sms_id in notification payload")

            // Fetch the message from the database
            val message = outboundSmsRepository.findById(smsId).orElse(null)

            if (message == null) {
                logger.warn("Message with ID $smsId not found in database")
                return
            }

            // Send notification via WebSocket
            webSocketService.notifyNewMessage(message)
            
            logger.info("New message notification processed and sent via WebSocket: SMS ID=$smsId, User=${message.person?.username}, AlertLevel=${message.alertLevel}")

        } catch (e: Exception) {
            logger.error("Error processing message notification: ${e.message}", e)
        }
    }

    /**
     * Data class for notification payload
     */
    data class OutboundSmsNotification(
        val smsId: Int,
        val personId: Int?,
        val alertLevel: Int
    )
}
