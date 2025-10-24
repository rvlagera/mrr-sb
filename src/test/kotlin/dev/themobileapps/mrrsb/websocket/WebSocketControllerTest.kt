package dev.themobileapps.mrrsb.websocket

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.Principal

/**
 * Unit tests for WebSocketController.
 */
class WebSocketControllerTest {
    
    private lateinit var notificationService: NotificationWebSocketService
    private lateinit var controller: WebSocketController
    private lateinit var principal: Principal
    
    @BeforeEach
    fun setup() {
        notificationService = mockk(relaxed = true)
        controller = WebSocketController(notificationService)
        principal = mockk()
        every { principal.name } returns "testuser"
    }
    
    @Test
    fun `subscribe should return subscription acknowledgment with user info`() {
        // When
        val result = controller.subscribe(principal)
        
        // Then
        assertEquals("subscribed", result["status"])
        assertEquals("testuser", result["user"])
        assertNotNull(result["timestamp"])
        assertTrue(result["timestamp"] is Long)
    }
    
    @Test
    fun `subscribe should handle anonymous user when principal is null`() {
        // When
        val result = controller.subscribe(null)
        
        // Then
        assertEquals("subscribed", result["status"])
        assertEquals("anonymous", result["user"])
        assertNotNull(result["timestamp"])
    }
    
    @Test
    fun `subscribe should include current timestamp`() {
        // Given
        val beforeTimestamp = System.currentTimeMillis()
        
        // When
        val result = controller.subscribe(principal)
        val afterTimestamp = System.currentTimeMillis()
        
        // Then
        val timestamp = result["timestamp"] as Long
        assertTrue(timestamp >= beforeTimestamp)
        assertTrue(timestamp <= afterTimestamp)
    }
    
    @Test
    fun `ping should return pong response`() {
        // When
        val result = controller.ping(principal)
        
        // Then
        assertEquals("pong", result["status"])
        assertNotNull(result["timestamp"])
        assertTrue(result["timestamp"] is Long)
    }
    
    @Test
    fun `ping should handle anonymous user when principal is null`() {
        // When
        val result = controller.ping(null)
        
        // Then
        assertEquals("pong", result["status"])
        assertNotNull(result["timestamp"])
    }
    
    @Test
    fun `ping should include current timestamp`() {
        // Given
        val beforeTimestamp = System.currentTimeMillis()
        
        // When
        val result = controller.ping(principal)
        val afterTimestamp = System.currentTimeMillis()
        
        // Then
        val timestamp = result["timestamp"] as Long
        assertTrue(timestamp >= beforeTimestamp)
        assertTrue(timestamp <= afterTimestamp)
    }
    
    @Test
    fun `sendHeartbeat should call notification service`() {
        // When
        controller.sendHeartbeat()
        
        // Then
        verify(exactly = 1) { notificationService.sendHeartbeat() }
    }
    
    @Test
    fun `sendHeartbeat should handle exceptions gracefully`() {
        // Given
        every { notificationService.sendHeartbeat() } throws RuntimeException("Test error")
        
        // When / Then - should not throw
        assertDoesNotThrow {
            controller.sendHeartbeat()
        }
    }
    
    @Test
    fun `subscribe should return different response for different users`() {
        // Given
        val principal1 = mockk<Principal>()
        val principal2 = mockk<Principal>()
        every { principal1.name } returns "user1"
        every { principal2.name } returns "user2"
        
        // When
        val result1 = controller.subscribe(principal1)
        val result2 = controller.subscribe(principal2)
        
        // Then
        assertEquals("user1", result1["user"])
        assertEquals("user2", result2["user"])
        assertNotEquals(result1["user"], result2["user"])
    }
    
    @Test
    fun `multiple ping calls should return different timestamps`() {
        // Given
        Thread.sleep(1) // Ensure different timestamps
        
        // When
        val result1 = controller.ping(principal)
        Thread.sleep(1)
        val result2 = controller.ping(principal)
        
        // Then
        val timestamp1 = result1["timestamp"] as Long
        val timestamp2 = result2["timestamp"] as Long
        assertTrue(timestamp2 >= timestamp1)
    }
}
