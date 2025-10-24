package dev.themobileapps.mrrsb.dto.response

data class AuthResponse(
    val token: String,
    val username: String,
    val name: String,
    val userId: Int,
    val expiresAt: Long
)
