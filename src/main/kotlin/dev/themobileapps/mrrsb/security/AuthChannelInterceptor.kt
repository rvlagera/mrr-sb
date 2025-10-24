package dev.themobileapps.mrrsb.security

import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

/**
 * Interceptor for WebSocket channel to authenticate JWT tokens
 */
@Component
class AuthChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(AuthChannelInterceptor::class.java)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(
            message,
            StompHeaderAccessor::class.java
        )

        if (accessor != null && StompCommand.CONNECT == accessor.command) {
            // Extract token from Authorization header
            val authToken = accessor.getFirstNativeHeader("Authorization")

            if (authToken != null) {
                val token = if (authToken.startsWith("Bearer ")) {
                    authToken.substring(7)
                } else {
                    authToken
                }

                // Validate token and set user principal
                if (jwtTokenProvider.validateToken(token)) {
                    try {
                        val username = jwtTokenProvider.getUsernameFromToken(token)
                        val userId = jwtTokenProvider.getUserIdFromToken(token)

                        val authentication = UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            emptyList()
                        )

                        accessor.user = authentication
                        accessor.setLeaveMutable(true)

                        logger.debug("WebSocket authenticated user: $username (ID: $userId)")
                    } catch (e: Exception) {
                        logger.error("Error extracting user from token: ${e.message}")
                    }
                } else {
                    logger.warn("Invalid JWT token in WebSocket connection")
                }
            } else {
                logger.warn("No Authorization header in WebSocket CONNECT frame")
            }
        }

        return message
    }
}
