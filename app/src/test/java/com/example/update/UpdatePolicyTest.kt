package com.example.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {
    @Test
    fun newerMandatoryVersionRequiresUpdate() {
        assertTrue(UpdatePolicy.requiresUpdate(currentVersionCode = 1, latestVersionCode = 2, mandatory = true))
    }

    @Test
    fun sameVersionDoesNotRequireUpdate() {
        assertFalse(UpdatePolicy.requiresUpdate(currentVersionCode = 2, latestVersionCode = 2, mandatory = true))
    }

    @Test
    fun olderOrOptionalVersionDoesNotBlockTheApp() {
        assertFalse(UpdatePolicy.requiresUpdate(currentVersionCode = 3, latestVersionCode = 2, mandatory = true))
        assertFalse(UpdatePolicy.requiresUpdate(currentVersionCode = 1, latestVersionCode = 2, mandatory = false))
    }
}
