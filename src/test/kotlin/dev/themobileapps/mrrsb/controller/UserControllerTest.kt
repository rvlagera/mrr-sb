package dev.themobileapps.mrrsb.controller

import dev.themobileapps.mrrsb.dto.request.UpdateProfileRequest
import dev.themobileapps.mrrsb.dto.response.UserProfileDto
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.exception.ResourceNotFoundException
import dev.themobileapps.mrrsb.security.CustomUserDetails
import dev.themobileapps.mrrsb.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService

    private val testPerson = Person(
        personId = 1,
        username = "testuser",
        password = "password",
        name = "Test User",
        mobileNo = "+1234567890",
        active = true,
        dateCreated = LocalDateTime.of(2024, 1, 1, 0, 0)
    )

    private val testProfile = UserProfileDto(
        userId = 1,
        username = "testuser",
        name = "Test User",
        mobileNo = "+1234567890",
        active = true,
        dateCreated = LocalDateTime.of(2024, 1, 1, 0, 0)
    )

    private fun setAuthentication() {
        val userDetails = CustomUserDetails(testPerson)
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `GET profile should return user profile when authenticated`() {
        // Given
        setAuthentication()
        `when`(userService.getUserProfile(1)).thenReturn(testProfile)

        // When & Then
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.name").value("Test User"))
            .andExpect(jsonPath("$.mobileNo").value("+1234567890"))
            .andExpect(jsonPath("$.active").value(true))
    }

    @Test
    fun `GET profile should return 401 when not authenticated`() {
        // When & Then
        SecurityContextHolder.clearContext()
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET profile should return 404 when user not found`() {
        // Given
        setAuthentication()
        `when`(userService.getUserProfile(1))
            .thenThrow(ResourceNotFoundException("User with ID 1 not found"))

        // When & Then
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("User with ID 1 not found"))
    }

    @Test
    fun `GET profile should handle user without mobile number`() {
        // Given
        setAuthentication()
        val profileWithoutMobile = testProfile.copy(mobileNo = null)
        `when`(userService.getUserProfile(1)).thenReturn(profileWithoutMobile)

        // When & Then
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.mobileNo").doesNotExist())
    }

    @Test
    fun `PUT profile should return 501 for update attempt due to read-only database`() {
        // Given
        setAuthentication()
        `when`(userService.updateProfile(any(), any()))
            .thenThrow(UnsupportedOperationException("Profile updates are not supported. The database is currently in read-only mode."))

        // When & Then
        mockMvc.perform(put("/users/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "Updated Name", "mobileNo": "+9876543210"}"""))
            .andExpect(status().isNotImplemented)
            .andExpect(jsonPath("$.error").value("NOT_IMPLEMENTED"))
            .andExpect(jsonPath("$.message").value("Profile updates are not supported. The database is currently in read-only mode."))
    }

    @Test
    fun `PUT profile should return 401 when not authenticated`() {
        // When & Then
        SecurityContextHolder.clearContext()
        mockMvc.perform(put("/users/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "Updated Name"}"""))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `PUT profile should return 400 for invalid mobile number format`() {
        // Given
        setAuthentication()

        // When & Then
        mockMvc.perform(put("/users/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"mobileNo": "invalid"}"""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
    }

    @Test
    fun `PUT profile should return 400 for name too long`() {
        // Given
        setAuthentication()

        // When & Then
        val longName = "a".repeat(201)
        mockMvc.perform(put("/users/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "$longName"}"""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
    }

    @Test
    fun `PUT profile should accept valid update request`() {
        // Given
        setAuthentication()
        `when`(userService.updateProfile(any(), any()))
            .thenThrow(UnsupportedOperationException("Profile updates are not supported. The database is currently in read-only mode."))

        // When & Then
        mockMvc.perform(put("/users/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "Valid Name", "mobileNo": "+1234567890"}"""))
            .andExpect(status().isNotImplemented)
    }
}
