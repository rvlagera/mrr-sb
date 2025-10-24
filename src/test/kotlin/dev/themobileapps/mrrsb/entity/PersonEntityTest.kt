package dev.themobileapps.mrrsb.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class PersonEntityTest {

    @Test
    fun `should create Person entity with required fields`() {
        // Given
        val username = "testuser"
        val password = "password123"
        val name = "Test User"

        // When
        val person = Person(
            personId = 1,
            username = username,
            password = password,
            name = name,
            mobileNo = "09171234567",
            active = true,
            dateCreated = LocalDateTime.now()
        )

        // Then
        assertNotNull(person)
        assertEquals(1, person.personId)
        assertEquals(username, person.username)
        assertEquals(password, person.password)
        assertEquals(name, person.name)
        assertEquals("09171234567", person.mobileNo)
        assertTrue(person.active)
        assertNotNull(person.dateCreated)
        assertTrue(person.outboundMessages.isEmpty())
    }

    @Test
    fun `should create Person with default values`() {
        // Given & When
        val person = Person(
            username = "testuser",
            password = "password123",
            name = "Test User"
        )

        // Then
        assertEquals(0, person.personId)
        assertNull(person.mobileNo)
        assertTrue(person.active)
        assertNull(person.dateCreated)
        assertTrue(person.outboundMessages.isEmpty())
    }

    @Test
    fun `should support inactive user`() {
        // Given & When
        val person = Person(
            username = "inactiveuser",
            password = "password123",
            name = "Inactive User",
            active = false
        )

        // Then
        assertFalse(person.active)
    }
}
