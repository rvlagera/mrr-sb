package dev.themobileapps.mrrsb.service

import dev.themobileapps.mrrsb.dto.request.UpdateProfileRequest
import dev.themobileapps.mrrsb.dto.response.UserProfileDto
import dev.themobileapps.mrrsb.entity.Person
import dev.themobileapps.mrrsb.exception.ResourceNotFoundException
import dev.themobileapps.mrrsb.repository.PersonRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing user profiles and user-related operations.
 *
 * @property personRepository Repository for accessing person data
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val personRepository: PersonRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Retrieves the profile information for a specific user.
     *
     * @param userId The ID of the user whose profile to retrieve
     * @return UserProfileDto containing the user's profile information
     * @throws ResourceNotFoundException if the user is not found
     */
    fun getUserProfile(userId: Int): UserProfileDto {
        logger.debug("Getting profile for user ID: {}", userId)

        val person = personRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User with ID $userId not found") }

        return convertToProfileDto(person)
    }

    /**
     * Updates the profile information for a specific user.
     *
     * Note: This implementation is prepared for future use. Currently, the database
     * is read-only, so this method throws an UnsupportedOperationException.
     * When write operations are enabled, this method will persist the changes.
     *
     * @param userId The ID of the user whose profile to update
     * @param request The update request containing the new profile information
     * @return UserProfileDto containing the updated user's profile information
     * @throws ResourceNotFoundException if the user is not found
     * @throws UnsupportedOperationException always (database is read-only)
     */
    @Transactional(readOnly = false)
    fun updateProfile(userId: Int, request: UpdateProfileRequest): UserProfileDto {
        logger.debug("Update profile requested for user ID: {}", userId)

        // Verify user exists
        val person = personRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User with ID $userId not found") }

        // Database is currently read-only, so we cannot persist changes
        // This implementation is prepared for future when write operations are enabled
        throw UnsupportedOperationException(
            "Profile updates are not supported. The database is currently in read-only mode."
        )

        // Future implementation (when write operations are enabled):
        // val updatedPerson = person.copy(
        //     name = request.name ?: person.name,
        //     mobileNo = request.mobileNo ?: person.mobileNo
        // )
        // val saved = personRepository.save(updatedPerson)
        // return convertToProfileDto(saved)
    }

    /**
     * Converts a Person entity to a UserProfileDto.
     *
     * @param person The person entity to convert
     * @return UserProfileDto representation of the person
     */
    private fun convertToProfileDto(person: Person): UserProfileDto {
        return UserProfileDto(
            userId = person.personId,
            username = person.username,
            name = person.name,
            mobileNo = person.mobileNo,
            active = person.active,
            dateCreated = person.dateCreated
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
