package dev.themobileapps.mrrsb.config

import dev.themobileapps.mrrsb.security.AuthChannelInterceptor
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.assertNotNull

@SpringBootTest
class WebSocketConfigTest {

    @Autowired
    private lateinit var webSocketConfig: WebSocketConfig

    @Autowired
    private lateinit var authChannelInterceptor: AuthChannelInterceptor

    @Test
    fun `should create WebSocketConfig bean`() {
        assertNotNull(webSocketConfig)
    }

    @Test
    fun `should configure message broker with correct prefixes`() {
        val registry = mock<MessageBrokerRegistry>()
        
        webSocketConfig.configureMessageBroker(registry)
        
        verify(registry).enableSimpleBroker("/topic", "/queue")
        verify(registry).setApplicationDestinationPrefixes("/app")
        verify(registry).setUserDestinationPrefix("/user")
    }

    @Test
    fun `should register STOMP endpoints with SockJS`() {
        val registry = mock<StompEndpointRegistry>()
        val registration = mock<org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration>()
        val sockJsRegistration = mock<org.springframework.web.socket.config.annotation.SockJsServiceRegistration>()
        
        org.mockito.kotlin.whenever(registry.addEndpoint("/ws")).thenReturn(registration)
        org.mockito.kotlin.whenever(registration.setAllowedOriginPatterns("*")).thenReturn(registration)
        org.mockito.kotlin.whenever(registration.withSockJS()).thenReturn(sockJsRegistration)
        
        webSocketConfig.registerStompEndpoints(registry)
        
        verify(registry).addEndpoint("/ws")
        verify(registration).setAllowedOriginPatterns("*")
        verify(registration).withSockJS()
    }

    @Test
    fun `should configure client inbound channel with auth interceptor`() {
        val registration = mock<ChannelRegistration>()
        
        webSocketConfig.configureClientInboundChannel(registration)
        
        verify(registration).interceptors(authChannelInterceptor)
    }
}
