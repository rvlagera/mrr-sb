package dev.themobileapps.mrrsb.websocket

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.security.Principal

/**
 * WebSocket controller handling client subscriptions and heartbeat messages.
 *
 * This controller provides endpoints for:
 * - Client subscription acknowledgment
 * - Ping/pong health checks
 * - Scheduled heartbeat broadcasts
 *
 * All endpoints require WebSocket authentication via JWT token.
 *
 * @property notificationService Service for sending WebSocket notifications
 */
@Controller
class WebSocketController(
    private val notificationService: NotificationWebSocketService
) {
    
    private val logger = LoggerFactory.getLogger(WebSocketController::class.java)
    
    /**
     * Handles client subscription requests.
     * Sends acknowledgment back to the specific user who subscribed.
     *
     * Clients should send a message to: /app/subscribe
     * Response will be sent to: /user/queue/subscribed
     *
     * @param principal The authenticated user principal from JWT
     * @return Map containing subscription status, username, and timestamp
     */
    @MessageMapping("/subscribe")
    @SendToUser("/queue/subscribed")
    fun subscribe(principal: Principal?): Map<String, Any> {
        val username = principal?.name ?: "anonymous"
        logger.debug("User subscribed: {}", username)
        
        return mapOf(
            "status" to "subscribed",
            "user" to username,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    /**
     * Handles ping requests from clients for connection health checks.
     * Responds with pong message to confirm connection is alive.
     *
     * Clients should send a message to: /app/ping
     * Response will be sent to: /user/queue/pong
     *
     * @param principal The authenticated user principal from JWT
     * @return Map containing pong status and timestamp
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    fun ping(principal: Principal?): Map<String, Any> {
        val username = principal?.name ?: "anonymous"
        logger.trace("Ping received from user: {}", username)
        
        return mapOf(
            "status" to "pong",
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    /**
     * Sends heartbeat messages to all connected clients every 30 seconds.
     * This helps maintain WebSocket connections and detect disconnections.
     *
     * Heartbeat is broadcast to: /topic/heartbeat
     *
     * Scheduled with:
     * - Fixed delay: 30000ms (30 seconds) between executions
     * - Initial delay: 30000ms before first execution
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    fun sendHeartbeat() {
        try {
            logger.trace("Sending scheduled heartbeat to all clients")
            notificationService.sendHeartbeat()
        } catch (e: Exception) {
            logger.error("Error sending heartbeat: {}", e.message, e)
        }
    }
}
