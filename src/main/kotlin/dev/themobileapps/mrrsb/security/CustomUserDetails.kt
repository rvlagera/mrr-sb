package dev.themobileapps.mrrsb.security

import dev.themobileapps.mrrsb.entity.Person
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Custom UserDetails implementation for Spring Security
 */
class CustomUserDetails(
    private val person: Person
) : UserDetails {

    val userId: Int
        get() = person.personId

    override fun getAuthorities(): Collection<GrantedAuthority> {
        // For now, all users have ROLE_USER
        // This can be extended later to support different roles
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String {
        return person.password
    }

    override fun getUsername(): String {
        return person.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return person.active
    }
}
