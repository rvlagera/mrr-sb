package dev.themobileapps.mrrsb.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class OutboundSmsEntityTest {

    @Test
    fun `should create OutboundSms entity with required fields`() {
        // Given
        val message = "Test SMS message"
        val mobileNo = "09171234567"
        val status = "PENDING"
        val requestedBy = "admin"
        val dateRequested = LocalDateTime.now()

        // When
        val sms = OutboundSms(
            smsId = 1,
            message = message,
            alertLevel = 1,
            dateRequested = dateRequested,
            mobileNo = mobileNo,
            status = status,
            requestedBy = requestedBy
        )

        // Then
        assertNotNull(sms)
        assertEquals(1, sms.smsId)
        assertEquals(message, sms.message)
        assertEquals(1, sms.alertLevel)
        assertEquals(dateRequested, sms.dateRequested)
        assertEquals(mobileNo, sms.mobileNo)
        assertEquals(status, sms.status)
        assertEquals(requestedBy, sms.requestedBy)
        assertNull(sms.dateSent)
        assertNull(sms.person)
    }

    @Test
    fun `should create OutboundSms with default alert level`() {
        // Given & When
        val sms = OutboundSms(
            message = "Test message",
            dateRequested = LocalDateTime.now(),
            mobileNo = "09171234567",
            status = "PENDING",
            requestedBy = "admin"
        )

        // Then
        assertEquals(0, sms.smsId)
        assertEquals(0, sms.alertLevel)
        assertNull(sms.dateSent)
    }

    @Test
    fun `should support sent SMS with dateSent`() {
        // Given
        val dateSent = LocalDateTime.now()

        // When
        val sms = OutboundSms(
            message = "Test message",
            dateRequested = LocalDateTime.now().minusHours(1),
            dateSent = dateSent,
            mobileNo = "09171234567",
            status = "SENT",
            requestedBy = "admin"
        )

        // Then
        assertEquals("SENT", sms.status)
        assertNotNull(sms.dateSent)
        assertEquals(dateSent, sms.dateSent)
    }

    @Test
    fun `should support high alert level messages`() {
        // Given & When
        val sms = OutboundSms(
            message = "CRITICAL ALERT",
            alertLevel = 3,
            dateRequested = LocalDateTime.now(),
            mobileNo = "09171234567",
            status = "PENDING",
            requestedBy = "system"
        )

        // Then
        assertEquals(3, sms.alertLevel)
        assertEquals("CRITICAL ALERT", sms.message)
    }

    @Test
    fun `should associate OutboundSms with Person`() {
        // Given
        val person = Person(
            personId = 1,
            username = "testuser",
            password = "password",
            name = "Test User"
        )

        // When
        val sms = OutboundSms(
            person = person,
            message = "Test message",
            dateRequested = LocalDateTime.now(),
            mobileNo = "09171234567",
            status = "PENDING",
            requestedBy = "admin"
        )

        // Then
        assertNotNull(sms.person)
        assertEquals(person, sms.person)
        assertEquals("testuser", sms.person?.username)
    }
}
