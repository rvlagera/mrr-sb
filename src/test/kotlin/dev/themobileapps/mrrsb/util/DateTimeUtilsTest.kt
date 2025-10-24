package dev.themobileapps.mrrsb.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Tests for DateTimeUtils
 */
class DateTimeUtilsTest {
    
    private val dateTimeUtils = DateTimeUtils()
    
    @Test
    fun `should adjust from database time by subtracting 8 hours`() {
        // Given
        val databaseTime = LocalDateTime.of(2024, 10, 24, 12, 0, 0)
        
        // When
        val adjusted = dateTimeUtils.adjustFromDatabaseTime(databaseTime)
        
        // Then
        assertThat(adjusted).isEqualTo(LocalDateTime.of(2024, 10, 24, 4, 0, 0))
    }
    
    @Test
    fun `should adjust to database time by adding 8 hours`() {
        // Given
        val utcTime = LocalDateTime.of(2024, 10, 24, 4, 0, 0)
        
        // When
        val adjusted = dateTimeUtils.adjustToDatabaseTime(utcTime)
        
        // Then
        assertThat(adjusted).isEqualTo(LocalDateTime.of(2024, 10, 24, 12, 0, 0))
    }
    
    @Test
    fun `should format relative time as just now for less than 1 minute`() {
        // Given
        val dateTime = LocalDateTime.now().minusSeconds(30)
        
        // When
        val formatted = dateTimeUtils.formatRelativeTime(dateTime)
        
        // Then
        assertThat(formatted).isEqualTo("just now")
    }
    
    @Test
    fun `should format relative time as minutes ago`() {
        // Given
        val dateTime = LocalDateTime.now().minusMinutes(5)
        
        // When
        val formatted = dateTimeUtils.formatRelativeTime(dateTime)
        
        // Then
        assertThat(formatted).isEqualTo("5 minutes ago")
    }
    
    @Test
    fun `should format relative time as hours ago`() {
        // Given
        val dateTime = LocalDateTime.now().minusHours(3)
        
        // When
        val formatted = dateTimeUtils.formatRelativeTime(dateTime)
        
        // Then
        assertThat(formatted).isEqualTo("3 hours ago")
    }
    
    @Test
    fun `should format relative time as days ago`() {
        // Given
        val dateTime = LocalDateTime.now().minusDays(2)
        
        // When
        val formatted = dateTimeUtils.formatRelativeTime(dateTime)
        
        // Then
        assertThat(formatted).isEqualTo("2 days ago")
    }
    
    @Test
    fun `should format relative time as ISO date for more than 7 days`() {
        // Given
        val dateTime = LocalDateTime.of(2024, 10, 1, 12, 0, 0)
        
        // When
        val formatted = dateTimeUtils.formatRelativeTime(dateTime)
        
        // Then
        assertThat(formatted).isEqualTo("2024-10-01")
    }
    
    @Test
    fun `should handle midnight crossing when adjusting from database time`() {
        // Given
        val databaseTime = LocalDateTime.of(2024, 10, 24, 2, 0, 0)
        
        // When
        val adjusted = dateTimeUtils.adjustFromDatabaseTime(databaseTime)
        
        // Then
        assertThat(adjusted).isEqualTo(LocalDateTime.of(2024, 10, 23, 18, 0, 0))
    }
    
    @Test
    fun `should handle midnight crossing when adjusting to database time`() {
        // Given
        val utcTime = LocalDateTime.of(2024, 10, 23, 20, 0, 0)
        
        // When
        val adjusted = dateTimeUtils.adjustToDatabaseTime(utcTime)
        
        // Then
        assertThat(adjusted).isEqualTo(LocalDateTime.of(2024, 10, 24, 4, 0, 0))
    }
}
