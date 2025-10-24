package dev.themobileapps.mrrsb.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

/**
 * Component responsible for generating and validating JWT tokens
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {

    private val signingKey: Key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Generate JWT token for a user
     * @param username The username
     * @param userId The user ID
     * @return Generated JWT token
     */
    fun generateToken(username: String, userId: Int): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * Validate JWT token
     * @param token The token to validate
     * @return true if valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get username from token
     * @param token The JWT token
     * @return The username
     */
    fun getUsernameFromToken(token: String): String {
        val claims = getClaims(token)
        return claims.subject
    }

    /**
     * Get user ID from token
     * @param token The JWT token
     * @return The user ID
     */
    fun getUserIdFromToken(token: String): Int {
        val claims = getClaims(token)
        return claims["userId"] as Int
    }

    /**
     * Get expiration date from token
     * @param token The JWT token
     * @return The expiration date
     */
    fun getExpirationDateFromToken(token: String): Date {
        val claims = getClaims(token)
        return claims.expiration
    }

    /**
     * Check if token is expired
     * @param token The JWT token
     * @return true if expired, false otherwise
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = getExpirationDateFromToken(token)
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Get claims from token
     * @param token The JWT token
     * @return The claims
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
