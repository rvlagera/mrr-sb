package dev.themobileapps.mrrsb.dto.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Data transfer object for updating user profile information.
 * Note: Database is currently read-only, so updates are not persisted.
 * This DTO is prepared for future when write operations are enabled.
 *
 * @property name The user's updated full name (optional)
 * @property mobileNo The user's updated mobile number (optional, must match phone number pattern)
 */
data class UpdateProfileRequest(
    @field:Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    val name: String? = null,

    @field:Pattern(
        regexp = "^[+]?[0-9]{10,20}$",
        message = "Mobile number must be 10-20 digits, optionally starting with +"
    )
    @field:Size(max = 20, message = "Mobile number must not exceed 20 characters")
    val mobileNo: String? = null
)
