package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StartupRestoreGateTest {
    @Test
    fun emptyInitialAuthCallbackDoesNotConsumePersistedUserRestore() {
        val gate = StartupRestoreGate()

        assertNull(gate.onAuthStateChanged(""))
        assertEquals("persisted-user", gate.onAuthStateChanged("persisted-user"))
        assertNull(gate.onAuthStateChanged("persisted-user"))
    }

    @Test
    fun signOutThenSignInToSameAccountTriggersAnotherRestore() {
        val gate = StartupRestoreGate()

        assertEquals("account-a", gate.onAuthStateChanged("account-a"))
        assertNull(gate.onAuthStateChanged(""))
        assertEquals("account-a", gate.onAuthStateChanged("account-a"))
    }

    @Test
    fun switchingAccountsRestoresOnlyTheNewActiveAccount() {
        val gate = StartupRestoreGate()

        assertEquals("account-a", gate.onAuthStateChanged("account-a"))
        assertEquals("account-b", gate.onAuthStateChanged("account-b"))
        assertNull(gate.onAuthStateChanged("account-b"))
    }
}
