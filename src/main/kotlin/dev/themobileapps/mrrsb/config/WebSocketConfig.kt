package dev.themobileapps.mrrsb.config

import dev.themobileapps.mrrsb.security.AuthChannelInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * Configuration for WebSocket with STOMP protocol
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val authChannelInterceptor: AuthChannelInterceptor
) : WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for pub/sub messaging
     */
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Enable simple in-memory broker for pub/sub
        config.enableSimpleBroker("/topic", "/queue")
        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app")
        // Set user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user")
    }

    /**
     * Register STOMP endpoints for WebSocket connections
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Register /ws endpoint with SockJS fallback
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    /**
     * Configure interceptors for client inbound channel
     */
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(authChannelInterceptor)
    }
}
