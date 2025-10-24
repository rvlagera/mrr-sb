package dev.themobileapps.mrrsb.dto.response

import java.time.LocalDateTime

/**
 * Data transfer object for outbound SMS message
 */
data class MessageDto(
    val id: Int,
    val message: String,
    val alertLevel: Int,
    val dateRequested: LocalDateTime,
    val dateSent: LocalDateTime?,
    val status: String,
    val mobileNo: String,
    val requestedBy: String
)
