package dev.themobileapps.mrrsb.security

import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.repository.PersonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.time.LocalDateTime

class CustomUserDetailsServiceTest {

    private lateinit var personRepository: PersonRepository
    private lateinit var userDetailsService: CustomUserDetailsService

    @BeforeEach
    fun setup() {
        personRepository = mockk()
        userDetailsService = CustomUserDetailsService(personRepository)
    }

    @Test
    fun `should load user by username successfully`() {
        // Given
        val username = "testuser"
        val person = Person(
            personId = 1,
            username = username,
            password = "password123",
            name = "Test User",
            mobileNo = "1234567890",
            active = true,
            dateCreated = LocalDateTime.now()
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        val userDetails = userDetailsService.loadUserByUsername(username)

        // Then
        assertNotNull(userDetails)
        assertEquals(username, userDetails.username)
        assertEquals("password123", userDetails.password)
        assertTrue(userDetails.isEnabled)
        verify { personRepository.findByUsernameAndActive(username, true) }
    }

    @Test
    fun `should throw UsernameNotFoundException when user not found`() {
        // Given
        val username = "nonexistent"
        every { personRepository.findByUsernameAndActive(username, true) } returns null

        // When & Then
        val exception = assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername(username)
        }

        assertEquals("User not found with username: $username", exception.message)
        verify { personRepository.findByUsernameAndActive(username, true) }
    }

    @Test
    fun `should return CustomUserDetails with correct userId`() {
        // Given
        val username = "testuser"
        val userId = 999
        val person = Person(
            personId = userId,
            username = username,
            password = "password",
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        val userDetails = userDetailsService.loadUserByUsername(username) as CustomUserDetails

        // Then
        assertEquals(userId, userDetails.userId)
    }

    @Test
    fun `should return enabled user when person is active`() {
        // Given
        val username = "activeuser"
        val person = Person(
            personId = 1,
            username = username,
            password = "password",
            name = "Active User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        val userDetails = userDetailsService.loadUserByUsername(username)

        // Then
        assertTrue(userDetails.isEnabled)
    }

    @Test
    fun `should only query for active users`() {
        // Given
        val username = "testuser"
        val person = Person(
            personId = 1,
            username = username,
            password = "password",
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        userDetailsService.loadUserByUsername(username)

        // Then
        verify { personRepository.findByUsernameAndActive(username, true) }
        verify(exactly = 0) { personRepository.findByUsernameAndActive(username, false) }
    }

    @Test
    fun `should have USER authority`() {
        // Given
        val username = "testuser"
        val person = Person(
            personId = 1,
            username = username,
            password = "password",
            name = "Test User",
            active = true
        )

        every { personRepository.findByUsernameAndActive(username, true) } returns person

        // When
        val userDetails = userDetailsService.loadUserByUsername(username)

        // Then
        val authorities = userDetails.authorities
        assertNotNull(authorities)
        assertEquals(1, authorities.size)
        assertTrue(authorities.any { it.authority == "ROLE_USER" })
    }
}
