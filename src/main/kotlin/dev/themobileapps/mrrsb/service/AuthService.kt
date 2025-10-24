package dev.themobileapps.mrrsb.service

import dev.themobileapps.mrrsb.dto.request.LoginRequest
import dev.themobileapps.mrrsb.dto.response.AuthResponse
import dev.themobileapps.mrrsb.repository.PersonRepository
import dev.themobileapps.mrrsb.security.JwtTokenProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val personRepository: PersonRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun authenticate(username: String, password: String): AuthResponse {
        val person = personRepository.findByUsernameAndActive(username, true)
            ?: throw BadCredentialsException("Invalid credentials")

        // Check password (support both plain text and bcrypt for migration)
        val passwordMatch = if (person.password.startsWith("\$2")) {
            passwordEncoder.matches(password, person.password)
        } else {
            password == person.password
        }

        if (!passwordMatch) {
            throw BadCredentialsException("Invalid credentials")
        }

        val token = jwtTokenProvider.generateToken(username, person.personId)

        return AuthResponse(
            token = token,
            username = username,
            name = person.name,
            userId = person.personId
        )
    }

    fun authenticate(request: LoginRequest): AuthResponse {
        return authenticate(request.username, request.password)
    }
}
