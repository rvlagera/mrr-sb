package dev.themobileapps.mrrsb.controller

import dev.themobileapps.mrrsb.dto.request.UpdateProfileRequest
import dev.themobileapps.mrrsb.dto.response.UserProfileDto
import dev.themobileapps.mrrsb.security.CustomUserDetails
import dev.themobileapps.mrrsb.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * REST controller for user profile management operations.
 * All endpoints in this controller require authentication.
 *
 * @property userService Service for user-related operations
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val userService: UserService
) {

    /**
     * Retrieves the profile information for the authenticated user.
     *
     * @param user The authenticated user details
     * @return ResponseEntity containing the user's profile information
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Retrieves the profile information for the authenticated user")
    fun getProfile(
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<UserProfileDto> {
        val profile = userService.getUserProfile(user.userId)
        return ResponseEntity.ok(profile)
    }

    /**
     * Updates the profile information for the authenticated user.
     *
     * Note: Currently returns 501 Not Implemented because the database is read-only.
     * This endpoint is prepared for future when write operations are enabled.
     *
     * @param user The authenticated user details
     * @param request The update request containing the new profile information
     * @return ResponseEntity containing the updated user's profile information
     */
    @PutMapping("/profile")
    @Operation(
        summary = "Update user profile",
        description = "Updates the profile information for the authenticated user. " +
                "Note: Currently not supported as database is read-only."
    )
    fun updateProfile(
        @AuthenticationPrincipal user: CustomUserDetails,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserProfileDto> {
        // This will throw UnsupportedOperationException due to read-only database
        // The GlobalExceptionHandler will convert this to a proper error response
        val profile = userService.updateProfile(user.userId, request)
        return ResponseEntity.ok(profile)
    }
}
