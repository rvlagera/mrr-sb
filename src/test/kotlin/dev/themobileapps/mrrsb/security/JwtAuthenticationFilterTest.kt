package dev.themobileapps.mrrsb.security

import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.repository.PersonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime

class JwtAuthenticationFilterTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var personRepository: PersonRepository
    private lateinit var userDetailsService: CustomUserDetailsService
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    private val testSecret = "test-secret-key-that-is-long-enough-for-hs512-algorithm-requirement"
    private val testExpiration = 3600000L

    @BeforeEach
    fun setup() {
        // Clear security context before each test
        SecurityContextHolder.clearContext()

        // Initialize real JWT token provider
        jwtTokenProvider = JwtTokenProvider(testSecret, testExpiration)

        // Mock repository
        personRepository = mockk()

        // Initialize real user details service with mocked repository
        userDetailsService = CustomUserDetailsService(personRepository)

        // Initialize filter with real provider and service
        jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenProvider, userDetailsService)

        // Mock servlet components
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        filterChain = mockk(relaxed = true)
    }

    @Test
    fun `should authenticate user with valid token`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)
        val person = Person(
            personId = userId,
            username = username,
            password = "password",
            name = "Test User",
            active = true
        )

        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals(username, authentication.name)
        assertTrue(authentication.isAuthenticated)
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should not authenticate with invalid token`() {
        // Given
        val invalidToken = "invalid.token.value"
        every { request.getHeader("Authorization") } returns "Bearer $invalidToken"

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should not authenticate with missing token`() {
        // Given
        every { request.getHeader("Authorization") } returns null

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should not authenticate with token without Bearer prefix`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)
        every { request.getHeader("Authorization") } returns token // Missing "Bearer " prefix

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should not authenticate when user not found`() {
        // Given
        val username = "nonexistent"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)

        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { personRepository.findByUsernameAndActive(username, true) } returns null

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should extract userId from authenticated user`() {
        // Given
        val username = "testuser"
        val userId = 999
        val token = jwtTokenProvider.generateToken(username, userId)
        val person = Person(
            personId = userId,
            username = username,
            password = "password",
            name = "Test User",
            active = true
        )

        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        val userDetails = authentication.principal as CustomUserDetails
        assertEquals(userId, userDetails.userId)
    }

    @Test
    fun `should handle expired token gracefully`() {
        // Given - create token with very short expiration
        val shortExpirationProvider = JwtTokenProvider(testSecret, -1000L)
        val username = "testuser"
        val userId = 123
        val expiredToken = shortExpirationProvider.generateToken(username, userId)

        every { request.getHeader("Authorization") } returns "Bearer $expiredToken"

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should always call filter chain even when authentication fails`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer invalid"

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify(exactly = 1) { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should set authentication details from request`() {
        // Given
        val username = "testuser"
        val userId = 123
        val token = jwtTokenProvider.generateToken(username, userId)
        val person = Person(
            personId = userId,
            username = username,
            password = "password",
            name = "Test User",
            active = true
        )

        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { personRepository.findByUsernameAndActive(username, true) } returns person
        every { request.remoteAddr } returns "127.0.0.1"

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertNotNull(authentication.details)
    }
}
