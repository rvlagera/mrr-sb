package dev.themobileapps.mrrsb.security

import dev.themobileapps.mrrsb.repository.PersonRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Custom UserDetailsService implementation for loading user-specific data
 */
@Service
class CustomUserDetailsService(
    private val personRepository: PersonRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val person = personRepository.findByUsernameAndActive(username, true)
            ?: throw UsernameNotFoundException("User not found with username: $username")

        return CustomUserDetails(person)
    }
}
