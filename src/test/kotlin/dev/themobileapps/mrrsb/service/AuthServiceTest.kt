package dev.themobileapps.mrrsb.service

import dev.themobileapps.mrrsb.dto.request.LoginRequest
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.repository.PersonRepository
import dev.themobileapps.mrrsb.security.JwtTokenProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Date

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    private lateinit var personRepository: PersonRepository

    @MockK
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    private lateinit var authService: AuthService

    @Test
    fun `should authenticate with valid plain text credentials`() {
        // Given
        val username = "testuser"
        val password = "password"
        val person = Person(
            personId = 1,
            username = username,
            password = password,
            name = "Test User",
            active = true
        )
        val expectedToken = "jwt.token.here"
        val expectedExpiry = Date(1_700_000_000_000)

        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { jwtTokenProvider.generateToken(username, 1) } returns expectedToken
        every { jwtTokenProvider.getExpirationDateFromToken(expectedToken) } returns expectedExpiry

        // When
        val result = authService.authenticate(username, password)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(username, result.username)
        assertEquals("Test User", result.name)
        assertEquals(1, result.userId)
        assertEquals(expectedExpiry.time, result.expiresAt)
        verify(exactly = 1) { personRepository.findByUsernameAndActive(username, true) }
        verify(exactly = 1) { jwtTokenProvider.generateToken(username, 1) }
        verify(exactly = 1) { jwtTokenProvider.getExpirationDateFromToken(expectedToken) }
    }

    @Test
    fun `should authenticate with valid bcrypt credentials`() {
        // Given
        val username = "testuser"
        val password = "password"
        val bcryptPassword = "\$2a\$10\$somehashedpassword"
        val person = Person(
            personId = 1,
            username = username,
            password = bcryptPassword,
            name = "Test User",
            active = true
        )
        val expectedToken = "jwt.token.here"
        val expectedExpiry = Date(1_700_000_100_000)

        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { passwordEncoder.matches(password, bcryptPassword) } returns true
        every { jwtTokenProvider.generateToken(username, 1) } returns expectedToken
        every { jwtTokenProvider.getExpirationDateFromToken(expectedToken) } returns expectedExpiry

        // When
        val result = authService.authenticate(username, password)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(username, result.username)
        assertEquals("Test User", result.name)
        assertEquals(1, result.userId)
        assertEquals(expectedExpiry.time, result.expiresAt)
        verify(exactly = 1) { personRepository.findByUsernameAndActive(username, true) }
        verify(exactly = 1) { passwordEncoder.matches(password, bcryptPassword) }
        verify(exactly = 1) { jwtTokenProvider.generateToken(username, 1) }
        verify(exactly = 1) { jwtTokenProvider.getExpirationDateFromToken(expectedToken) }
    }

    @Test
    fun `should authenticate with LoginRequest`() {
        // Given
        val request = LoginRequest(username = "testuser", password = "password")
        val person = Person(
            personId = 1,
            username = request.username,
            password = request.password,
            name = "Test User",
            active = true
        )
        val expectedToken = "jwt.token.here"
        val expectedExpiry = Date(1_700_000_200_000)

        every { personRepository.findByUsernameAndActive(request.username, true) } returns person
        every { jwtTokenProvider.generateToken(request.username, 1) } returns expectedToken
        every { jwtTokenProvider.getExpirationDateFromToken(expectedToken) } returns expectedExpiry

        // When
        val result = authService.authenticate(request)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(request.username, result.username)
        assertEquals("Test User", result.name)
        assertEquals(1, result.userId)
        assertEquals(expectedExpiry.time, result.expiresAt)
    }

    @Test
    fun `should throw BadCredentialsException when user not found`() {
        // Given
        val username = "nonexistent"
        val password = "password"

        every { personRepository.findByUsernameAndActive(username, true) } returns null

        // When & Then
        val exception = assertThrows<BadCredentialsException> {
            authService.authenticate(username, password)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(exactly = 1) { personRepository.findByUsernameAndActive(username, true) }
        verify(exactly = 0) { jwtTokenProvider.generateToken(any(), any()) }
    }

    @Test
    fun `should throw BadCredentialsException when user is inactive`() {
        // Given
        val username = "testuser"
        val password = "password"

        every { personRepository.findByUsernameAndActive(username, true) } returns null

        // When & Then
        val exception = assertThrows<BadCredentialsException> {
            authService.authenticate(username, password)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(exactly = 1) { personRepository.findByUsernameAndActive(username, true) }
    }

    @Test
    fun `should throw BadCredentialsException when plain text password is incorrect`() {
        // Given
        val username = "testuser"
        val password = "wrongpassword"
        val person = Person(
            personId = 1,
            username = username,
            password = "correctpassword",
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When & Then
        val exception = assertThrows<BadCredentialsException> {
            authService.authenticate(username, password)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(exactly = 1) { personRepository.findByUsernameAndActive(username, true) }
        verify(exactly = 0) { jwtTokenProvider.generateToken(any(), any()) }
    }

    @Test
    fun `should throw BadCredentialsException when bcrypt password is incorrect`() {
        // Given
        val username = "testuser"
        val password = "wrongpassword"
        val bcryptPassword = "\$2a\$10\$somehashedpassword"
        val person = Person(
            personId = 1,
            username = username,
            password = bcryptPassword,
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { passwordEncoder.matches(password, bcryptPassword) } returns false

        // When & Then
        val exception = assertThrows<BadCredentialsException> {
            authService.authenticate(username, password)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(exactly = 1) { personRepository.findByUsernameAndActive(username, true) }
        verify(exactly = 1) { passwordEncoder.matches(password, bcryptPassword) }
        verify(exactly = 0) { jwtTokenProvider.generateToken(any(), any()) }
    }

    @Test
    fun `should handle user with mobile number`() {
        // Given
        val username = "testuser"
        val password = "password"
        val person = Person(
            personId = 1,
            username = username,
            password = password,
            name = "Test User",
            mobileNo = "+1234567890",
            active = true,
            dateCreated = LocalDateTime.now()
        )
        val expectedToken = "jwt.token.here"
        val expectedExpiry = Date(1_700_000_300_000)

        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { jwtTokenProvider.generateToken(username, 1) } returns expectedToken
        every { jwtTokenProvider.getExpirationDateFromToken(expectedToken) } returns expectedExpiry

        // When
        val result = authService.authenticate(username, password)

        // Then
        assertNotNull(result)
        assertEquals(expectedToken, result.token)
        assertEquals(username, result.username)
        assertEquals("Test User", result.name)
        assertEquals(1, result.userId)
        assertEquals(expectedExpiry.time, result.expiresAt)
    }

    @Test
    fun `should distinguish between bcrypt and plain text passwords correctly`() {
        // Given - password starting with $2 should be treated as bcrypt
        val username = "testuser"
        val password = "password"
        val bcryptPassword = "\$2y\$10\$somehashedpassword"
        val person = Person(
            personId = 1,
            username = username,
            password = bcryptPassword,
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { passwordEncoder.matches(password, bcryptPassword) } returns true
        every { jwtTokenProvider.generateToken(username, 1) } returns "token"
        every { jwtTokenProvider.getExpirationDateFromToken("token") } returns Date(1_700_000_400_000)

        // When
        authService.authenticate(username, password)

        // Then
        verify(exactly = 1) { passwordEncoder.matches(password, bcryptPassword) }
    }

    @Test
    fun `should handle plain text password that does not start with dollar2`() {
        // Given - password not starting with $2 should be treated as plain text
        val username = "testuser"
        val password = "plainpassword"
        val person = Person(
            personId = 1,
            username = username,
            password = "plainpassword",
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { jwtTokenProvider.generateToken(username, 1) } returns "token"
        every { jwtTokenProvider.getExpirationDateFromToken("token") } returns Date(1_700_000_500_000)

        // When
        authService.authenticate(username, password)

        // Then
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
    }
}
