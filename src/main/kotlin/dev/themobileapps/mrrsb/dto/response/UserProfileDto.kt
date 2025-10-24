package dev.themobileapps.mrrsb.dto.response

import java.time.LocalDateTime

/**
 * Data transfer object for user profile information.
 * Contains basic user details excluding sensitive information like passwords.
 *
 * @property userId The unique identifier of the user
 * @property username The user's username
 * @property name The user's full name
 * @property mobileNo The user's mobile number (optional)
 * @property active Whether the user account is active
 * @property dateCreated The date when the user account was created
 */
data class UserProfileDto(
    val userId: Int,
    val username: String,
    val name: String,
    val mobileNo: String? = null,
    val active: Boolean,
    val dateCreated: LocalDateTime? = null
)
