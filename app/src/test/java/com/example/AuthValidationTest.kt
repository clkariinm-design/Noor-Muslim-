package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun rejectsInvalidEmailAddress() {
        assertEquals(
            "Enter a valid email address.",
            validateCredentials("not-an-email", "long-enough-password", true)
        )
    }

    @Test
    fun rejectsShortRegistrationPassword() {
        assertEquals(
            "Password must contain at least 10 characters.",
            validateCredentials("reader@example.com", "short", true)
        )
    }

    @Test
    fun acceptsValidRegistrationCredentials() {
        assertNull(
            validateCredentials("reader@example.com", "a-secure-passphrase", true)
        )
    }

    @Test
    fun rejectsBlankLoginPassword() {
        assertEquals(
            "Enter your password.",
            validateCredentials("reader@example.com", "", false)
        )
    }
}
