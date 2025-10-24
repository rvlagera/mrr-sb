package dev.themobileapps.mrrsb.util

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility component for date and time operations
 * 
 * Handles timezone conversion for database timestamps that are stored in UTC+8
 * without timezone information
 */
@Component
class DateTimeUtils {
    
    // Database stores timestamps in UTC+8 without timezone info
    private val DATABASE_OFFSET_HOURS = 8L
    
    /**
     * Adjusts LocalDateTime from database time (UTC+8) to actual UTC
     * 
     * Since the database stores timestamps in UTC+8 without timezone info,
     * we subtract 8 hours to get the actual UTC time
     * 
     * @param localDateTime The datetime from the database
     * @return Adjusted datetime in UTC
     */
    fun adjustFromDatabaseTime(localDateTime: LocalDateTime): LocalDateTime {
        // Subtract 8 hours to get actual UTC time
        return localDateTime.minusHours(DATABASE_OFFSET_HOURS)
    }
    
    /**
     * Adjusts LocalDateTime to database time (UTC+8) for writing
     * 
     * @param localDateTime The UTC datetime to store
     * @return Adjusted datetime for database storage
     */
    fun adjustToDatabaseTime(localDateTime: LocalDateTime): LocalDateTime {
        // Add 8 hours when writing to database
        return localDateTime.plusHours(DATABASE_OFFSET_HOURS)
    }
    
    /**
     * Formats a relative time string (e.g., "5 minutes ago", "2 hours ago")
     * 
     * @param dateTime The datetime to format
     * @return Human-readable relative time string
     */
    fun formatRelativeTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(dateTime, now)
        
        return when {
            duration.toMinutes() < 1 -> "just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            else -> dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }
}
