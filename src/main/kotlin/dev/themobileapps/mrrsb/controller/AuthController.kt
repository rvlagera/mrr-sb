package dev.themobileapps.mrrsb.controller

import dev.themobileapps.mrrsb.dto.request.LoginRequest
import dev.themobileapps.mrrsb.dto.response.AuthResponse
import dev.themobileapps.mrrsb.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.authenticate(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Clear any server-side session if needed")
    fun logout(): ResponseEntity<Map<String, String>> {
        // Since we're using stateless JWT authentication, logout is primarily client-side
        // The client should discard the JWT token
        // This endpoint is here for future server-side token blacklisting if needed
        return ResponseEntity.ok(mapOf("message" to "Logout successful"))
    }
}
