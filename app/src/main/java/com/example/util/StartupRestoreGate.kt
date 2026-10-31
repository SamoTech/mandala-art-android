package com.example.util

/**
 * Detects the first non-empty Firebase UID after initialization and subsequent
 * account sign-ins. Empty callbacks do not consume the initial restore event.
 */
internal class StartupRestoreGate {
    private var activeUid: String? = null

    /**
     * Returns the UID that should be restored, or null when no restore is due.
     * A signed-out callback resets the gate so signing into the same account
     * later triggers a fresh restore.
     */
    fun onAuthStateChanged(uid: String): String? {
        if (uid.isBlank()) {
            activeUid = null
            return null
        }
        if (uid == activeUid) return null
        activeUid = uid
        return uid
    }
}
