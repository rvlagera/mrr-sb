package dev.themobileapps.mrrsb.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private val testSecret = "test-secret-key-that-is-long-enough-for-hs512-algorithm-requirement"
    private val testExpiration = 3600000L // 1 hour

    @BeforeEach
    fun setup() {
        jwtTokenProvider = JwtTokenProvider(testSecret, testExpiration)
    }

    @Test
    fun `should generate token with valid username and userId`() {
        // Given
        val username = "testuser"
        val userId = 123

        // When
        val token = jwtTokenProvider.generateToken(username, userId)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.contains(".")) // JWT format has dots
    }

    @Test
    fun `should validate valid token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)

        // When
        val isValid = jwtTokenProvider.validateToken(token)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `should not validate invalid token`() {
        // Given
        val invalidToken = "invalid.token.value"

        // When
        val isValid = jwtTokenProvider.validateToken(invalidToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should not validate empty token`() {
        // Given
        val emptyToken = ""

        // When
        val isValid = jwtTokenProvider.validateToken(emptyToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should extract username from token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)

        // When
        val extractedUsername = jwtTokenProvider.getUsernameFromToken(token)

        // Then
        assertEquals(username, extractedUsername)
    }

    @Test
    fun `should extract userId from token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)

        // When
        val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should extract expiration date from token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val beforeGeneration = Date()
        val token = jwtTokenProvider.generateToken(username, userId)
        val afterGeneration = Date()

        // When
        val expirationDate = jwtTokenProvider.getExpirationDateFromToken(token)

        // Then
        assertNotNull(expirationDate)
        assertTrue(expirationDate.after(beforeGeneration))
        // Expiration should be roughly now + testExpiration
        val expectedExpiration = Date(afterGeneration.time + testExpiration)
        assertTrue(Math.abs(expirationDate.time - expectedExpiration.time) < 1000) // Within 1 second
    }

    @Test
    fun `should not be expired for newly generated token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)

        // When
        val isExpired = jwtTokenProvider.isTokenExpired(token)

        // Then
        assertFalse(isExpired)
    }

    @Test
    fun `should detect expired token`() {
        // Given - create provider with very short expiration
        val shortExpirationProvider = JwtTokenProvider(testSecret, -1000L) // Expired 1 second ago
        val username = "testuser"
        val userId = 123
        val token = shortExpirationProvider.generateToken(username, userId)

        // When
        val isExpired = shortExpirationProvider.isTokenExpired(token)

        // Then
        assertTrue(isExpired)
    }

    @Test
    fun `should handle different usernames correctly`() {
        // Given
        val username1 = "user1"
        val username2 = "user2"
        val userId = 123

        // When
        val token1 = jwtTokenProvider.generateToken(username1, userId)
        val token2 = jwtTokenProvider.generateToken(username2, userId)

        // Then
        assertNotEquals(token1, token2)
        assertEquals(username1, jwtTokenProvider.getUsernameFromToken(token1))
        assertEquals(username2, jwtTokenProvider.getUsernameFromToken(token2))
    }

    @Test
    fun `should handle different userIds correctly`() {
        // Given
        val username = "testuser"
        val userId1 = 123
        val userId2 = 456

        // When
        val token1 = jwtTokenProvider.generateToken(username, userId1)
        val token2 = jwtTokenProvider.generateToken(username, userId2)

        // Then
        assertNotEquals(token1, token2)
        assertEquals(userId1, jwtTokenProvider.getUserIdFromToken(token1))
        assertEquals(userId2, jwtTokenProvider.getUserIdFromToken(token2))
    }

    @Test
    fun `should generate unique tokens for same user at different times`() {
        // Given
        val username = "testuser"
        val userId = 123

        // When
        val token1 = jwtTokenProvider.generateToken(username, userId)
        Thread.sleep(1001) // Delay to ensure different timestamp (JWT uses seconds precision)
        val token2 = jwtTokenProvider.generateToken(username, userId)

        // Then
        assertNotEquals(token1, token2) // Different timestamps should produce different tokens
    }

    @Test
    fun `should validate token with special characters in username`() {
        // Given
        val username = "test.user+123@example.com"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)

        // When
        val isValid = jwtTokenProvider.validateToken(token)
        val extractedUsername = jwtTokenProvider.getUsernameFromToken(token)

        // Then
        assertTrue(isValid)
        assertEquals(username, extractedUsername)
    }

    @Test
    fun `should not validate tampered token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)
        val tamperedToken = token.substring(0, token.length - 5) + "XXXXX"

        // When
        val isValid = jwtTokenProvider.validateToken(tamperedToken)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `should handle token with zero userId`() {
        // Given
        val username = "testuser"
        val userId = 0

        // When
        val token = jwtTokenProvider.generateToken(username, userId)
        val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should handle token with negative userId`() {
        // Given
        val username = "testuser"
        val userId = -1

        // When
        val token = jwtTokenProvider.generateToken(username, userId)
        val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }
}
