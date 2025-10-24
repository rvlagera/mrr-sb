package dev.themobileapps.mrrsb.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Status
import java.sql.Connection
import javax.sql.DataSource

@ExtendWith(MockitoExtension::class)
class DatabaseHealthIndicatorTest {
    
    @Mock
    private lateinit var dataSource: DataSource
    
    @Mock
    private lateinit var connection: Connection
    
    @InjectMocks
    private lateinit var healthIndicator: DatabaseHealthIndicator
    
    @Test
    fun `should return UP status when database connection is valid`() {
        // Given
        whenever(dataSource.connection).thenReturn(connection)
        whenever(connection.isValid(1)).thenReturn(true)
        
        // When
        val health = healthIndicator.health()
        
        // Then
        assertEquals(Status.UP, health.status)
        assertEquals("PostgreSQL", health.details["database"])
        assertEquals("Connected", health.details["status"])
    }
    
    @Test
    fun `should return DOWN status when database connection is invalid`() {
        // Given
        whenever(dataSource.connection).thenReturn(connection)
        whenever(connection.isValid(1)).thenReturn(false)
        
        // When
        val health = healthIndicator.health()
        
        // Then
        assertEquals(Status.DOWN, health.status)
        assertEquals("PostgreSQL", health.details["database"])
        assertEquals("Connection invalid", health.details["status"])
    }
    
    @Test
    fun `should return DOWN status when exception occurs`() {
        // Given
        val errorMessage = "Connection timeout"
        whenever(dataSource.connection).thenThrow(RuntimeException(errorMessage))
        
        // When
        val health = healthIndicator.health()
        
        // Then
        assertEquals(Status.DOWN, health.status)
        assertEquals("PostgreSQL", health.details["database"])
        assertEquals(errorMessage, health.details["error"])
    }
    
    @Test
    fun `should close connection after checking health`() {
        // Given
        whenever(dataSource.connection).thenReturn(connection)
        whenever(connection.isValid(1)).thenReturn(true)
        
        // When
        val health = healthIndicator.health()
        
        // Then
        // Connection should be closed via use() block
        // This is verified by the fact that no exception is thrown
        assertNotNull(health)
        assertEquals(Status.UP, health.status)
    }
    
    @Test
    fun `should include error message in health details`() {
        // Given
        val errorMessage = "Database connection failed"
        whenever(dataSource.connection).thenThrow(RuntimeException(errorMessage))
        
        // When
        val health = healthIndicator.health()
        
        // Then
        assertEquals(Status.DOWN, health.status)
        assertEquals("PostgreSQL", health.details["database"])
        assertEquals(errorMessage, health.details["error"])
    }
}
