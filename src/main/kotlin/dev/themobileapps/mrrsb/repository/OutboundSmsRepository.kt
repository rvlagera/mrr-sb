package dev.themobileapps.mrrsb.repository

import dev.themobileapps.mrrsb.entity.OutboundSms
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository interface for OutboundSms entity
 * Provides database operations for OutboundSms table
 */
@Repository
interface OutboundSmsRepository : JpaRepository<OutboundSms, Int> {

    /**
     * Find all messages for a person, ordered by date requested descending
     * @param personId the person ID
     * @param pageable pagination information
     * @return Page of OutboundSms messages
     */
    fun findByPersonPersonIdOrderByDateRequestedDesc(
        personId: Int,
        pageable: Pageable
    ): Page<OutboundSms>

    /**
     * Find all messages for a person after a specific date, ordered by date requested descending
     * @param personId the person ID
     * @param after the date to filter from
     * @return List of OutboundSms messages
     */
    fun findByPersonPersonIdAndDateRequestedAfterOrderByDateRequestedDesc(
        personId: Int,
        after: LocalDateTime
    ): List<OutboundSms>

    /**
     * Count unread messages (messages without a sent date) for a person
     * @param personId the person ID
     * @return count of unread messages
     */
    fun countByPersonPersonIdAndDateSentIsNull(personId: Int): Long
}
