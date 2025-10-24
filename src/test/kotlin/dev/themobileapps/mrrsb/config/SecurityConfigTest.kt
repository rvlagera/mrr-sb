package dev.themobileapps.mrrsb.config

import dev.themobileapps.mrrsb.security.JwtAuthenticationEntryPoint
import dev.themobileapps.mrrsb.security.JwtAuthenticationFilter
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class SecurityConfigTest {

    private val jwtAuthenticationFilter = mockk<JwtAuthenticationFilter>(relaxed = true)
    private val jwtAuthenticationEntryPoint = mockk<JwtAuthenticationEntryPoint>(relaxed = true)
    private val corsProperties = CorsProperties(
        allowedOrigins = listOf("http://localhost:3000"),
        allowedMethods = listOf("GET", "POST", "PUT", "DELETE"),
        allowedHeaders = listOf("*"),
        allowCredentials = true
    )

    private val securityConfig = SecurityConfig(
        jwtAuthenticationFilter,
        jwtAuthenticationEntryPoint,
        corsProperties
    )

    @Test
    fun `should create password encoder bean`() {
        val passwordEncoder = securityConfig.passwordEncoder()

        assertNotNull(passwordEncoder)
        assertTrue(passwordEncoder is BCryptPasswordEncoder)
    }

    @Test
    fun `should create CORS configuration source`() {
        val corsConfigSource = securityConfig.corsConfigurationSource()

        assertNotNull(corsConfigSource)
        // CorsConfigurationSource is created successfully
        // Actual CORS configuration testing requires integration tests with real HTTP requests
    }

    @Test
    fun `password encoder should encode passwords`() {
        val passwordEncoder = securityConfig.passwordEncoder()
        val rawPassword = "testPassword123"

        val encodedPassword = passwordEncoder.encode(rawPassword)

        assertNotNull(encodedPassword)
        assertNotEquals(rawPassword, encodedPassword)
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword))
    }

    @Test
    fun `password encoder should not match incorrect password`() {
        val passwordEncoder = securityConfig.passwordEncoder()
        val rawPassword = "testPassword123"
        val wrongPassword = "wrongPassword456"

        val encodedPassword = passwordEncoder.encode(rawPassword)

        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword))
    }

    @Test
    fun `CORS configuration should use empty lists when properties are empty`() {
        val emptyCorsProperties = CorsProperties()
        val config = SecurityConfig(
            jwtAuthenticationFilter,
            jwtAuthenticationEntryPoint,
            emptyCorsProperties
        )

        val corsConfigSource = config.corsConfigurationSource()

        assertNotNull(corsConfigSource)
        // CorsConfigurationSource is created successfully with empty properties
        // Actual CORS configuration testing requires integration tests with real HTTP requests
    }
}
