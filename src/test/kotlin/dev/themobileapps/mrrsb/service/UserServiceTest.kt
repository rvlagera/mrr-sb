package dev.themobileapps.mrrsb.service

import dev.themobileapps.mrrsb.dto.request.UpdateProfileRequest
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.exception.ResourceNotFoundException
import dev.themobileapps.mrrsb.repository.PersonRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var personRepository: PersonRepository

    @InjectMockKs
    private lateinit var userService: UserService

    private val testPerson = Person(
        personId = 1,
        username = "testuser",
        password = "password123",
        name = "Test User",
        mobileNo = "+1234567890",
        active = true,
        dateCreated = LocalDateTime.of(2024, 1, 1, 0, 0)
    )

    @Test
    fun `getUserProfile should return user profile when user exists`() {
        // Given
        every { personRepository.findById(1) } returns Optional.of(testPerson)

        // When
        val result = userService.getUserProfile(1)

        // Then
        assertNotNull(result)
        assertEquals(1, result.userId)
        assertEquals("testuser", result.username)
        assertEquals("Test User", result.name)
        assertEquals("+1234567890", result.mobileNo)
        assertTrue(result.active)
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), result.dateCreated)
    }

    @Test
    fun `getUserProfile should throw exception when user not found`() {
        // Given
        every { personRepository.findById(999) } returns Optional.empty()

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            userService.getUserProfile(999)
        }
        assertEquals("User with ID 999 not found", exception.message)
    }

    @Test
    fun `getUserProfile should handle user without mobile number`() {
        // Given
        val personWithoutMobile = testPerson.copy(mobileNo = null)
        every { personRepository.findById(1) } returns Optional.of(personWithoutMobile)

        // When
        val result = userService.getUserProfile(1)

        // Then
        assertNotNull(result)
        assertNull(result.mobileNo)
    }

    @Test
    fun `getUserProfile should handle user without date created`() {
        // Given
        val personWithoutDate = testPerson.copy(dateCreated = null)
        every { personRepository.findById(1) } returns Optional.of(personWithoutDate)

        // When
        val result = userService.getUserProfile(1)

        // Then
        assertNotNull(result)
        assertNull(result.dateCreated)
    }

    @Test
    fun `getUserProfile should handle inactive user`() {
        // Given
        val inactivePerson = testPerson.copy(active = false)
        every { personRepository.findById(1) } returns Optional.of(inactivePerson)

        // When
        val result = userService.getUserProfile(1)

        // Then
        assertNotNull(result)
        assertFalse(result.active)
    }

    @Test
    fun `updateProfile should throw UnsupportedOperationException because database is read-only`() {
        // Given
        every { personRepository.findById(1) } returns Optional.of(testPerson)
        val updateRequest = UpdateProfileRequest(
            name = "Updated Name",
            mobileNo = "+9876543210"
        )

        // When & Then
        val exception = assertThrows(UnsupportedOperationException::class.java) {
            userService.updateProfile(1, updateRequest)
        }
        assertTrue(exception.message?.contains("read-only") ?: false)
    }

    @Test
    fun `updateProfile should throw exception when user not found`() {
        // Given
        every { personRepository.findById(999) } returns Optional.empty()
        val updateRequest = UpdateProfileRequest(name = "Updated Name")

        // When & Then
        assertThrows(ResourceNotFoundException::class.java) {
            userService.updateProfile(999, updateRequest)
        }
    }

    @Test
    fun `getUserProfile should return correct profile for multiple users`() {
        // Given
        val person1 = testPerson
        val person2 = testPerson.copy(
            personId = 2,
            username = "user2",
            name = "User Two",
            mobileNo = "+1111111111"
        )
        every { personRepository.findById(1) } returns Optional.of(person1)
        every { personRepository.findById(2) } returns Optional.of(person2)

        // When
        val profile1 = userService.getUserProfile(1)
        val profile2 = userService.getUserProfile(2)

        // Then
        assertEquals(1, profile1.userId)
        assertEquals("testuser", profile1.username)
        assertEquals(2, profile2.userId)
        assertEquals("user2", profile2.username)
    }
}
