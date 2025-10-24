package dev.themobileapps.mrrsb.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthChannelInterceptorTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authChannelInterceptor: AuthChannelInterceptor
    private lateinit var messageChannel: MessageChannel

    @BeforeEach
    fun setup() {
        jwtTokenProvider = mock()
        authChannelInterceptor = AuthChannelInterceptor(jwtTokenProvider)
        messageChannel = mock()
    }

    @Test
    fun `should authenticate valid JWT token on CONNECT`() {
        // Given
        val token = "valid.jwt.token"
        val username = "testuser"
        val userId = 1

        whenever(jwtTokenProvider.validateToken(token)).thenReturn(true)
        whenever(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username)
        whenever(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId)

        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        accessor.setNativeHeader("Authorization", "Bearer $token")
        accessor.setLeaveMutable(true)
        val message: Message<*> = MessageBuilder.createMessage(
            ByteArray(0),
            accessor.messageHeaders
        )

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
        // The accessor should have user set
        assertNotNull(accessor.user)
        assertEquals(username, (accessor.user as UsernamePasswordAuthenticationToken).principal)
    }

    @Test
    fun `should authenticate token without Bearer prefix`() {
        // Given
        val token = "valid.jwt.token"
        val username = "testuser"
        val userId = 1

        whenever(jwtTokenProvider.validateToken(token)).thenReturn(true)
        whenever(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username)
        whenever(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId)

        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        accessor.setNativeHeader("Authorization", token)
        accessor.setLeaveMutable(true)
        val message: Message<*> = MessageBuilder.createMessage(
            ByteArray(0),
            accessor.messageHeaders
        )

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
        assertNotNull(accessor.user)
    }

    @Test
    fun `should not authenticate invalid JWT token`() {
        // Given
        val token = "invalid.jwt.token"

        whenever(jwtTokenProvider.validateToken(token)).thenReturn(false)

        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        accessor.setNativeHeader("Authorization", "Bearer $token")
        accessor.setLeaveMutable(true)
        val message: Message<*> = MessageBuilder.createMessage(
            ByteArray(0),
            accessor.messageHeaders
        )

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
        assertNull(accessor.user)
    }

    @Test
    fun `should handle missing Authorization header`() {
        // Given
        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        accessor.setLeaveMutable(true)
        val message: Message<*> = MessageBuilder.createMessage(
            ByteArray(0),
            accessor.messageHeaders
        )

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
        assertNull(accessor.user)
    }

    @Test
    fun `should handle non-CONNECT commands`() {
        // Given
        val accessor = StompHeaderAccessor.create(StompCommand.SEND)
        accessor.setNativeHeader("Authorization", "Bearer valid.token")
        val message: Message<*> = MessageBuilder.createMessage(
            ByteArray(0),
            accessor.messageHeaders
        )

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `should handle token extraction errors gracefully`() {
        // Given
        val token = "valid.jwt.token"

        whenever(jwtTokenProvider.validateToken(token)).thenReturn(true)
        whenever(jwtTokenProvider.getUsernameFromToken(token)).thenThrow(RuntimeException("Token parsing error"))

        val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
        accessor.setNativeHeader("Authorization", "Bearer $token")
        accessor.setLeaveMutable(true)
        val message: Message<*> = MessageBuilder.createMessage(
            ByteArray(0),
            accessor.messageHeaders
        )

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
        assertNull(accessor.user)
    }

    @Test
    fun `should pass through message for null accessor`() {
        // Given
        val message: Message<*> = MessageBuilder.withPayload(ByteArray(0)).build()

        // When
        val result = authChannelInterceptor.preSend(message, messageChannel)

        // Then
        assertNotNull(result)
        assertEquals(message, result)
    }
}
