package dev.themobileapps.mrrsb.repository

import dev.themobileapps.mrrsb.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for Person entity
 * Provides database operations for Person table
 */
@Repository
interface PersonRepository : JpaRepository<Person, Int> {

    /**
     * Find a person by username
     * @param username the username to search for
     * @return Person if found, null otherwise
     */
    fun findByUsername(username: String): Person?

    /**
     * Find a person by username and active status
     * @param username the username to search for
     * @param active the active status
     * @return Person if found, null otherwise
     */
    fun findByUsernameAndActive(username: String, active: Boolean): Person?
}
