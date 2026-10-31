package com.example.ui.editor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serial, conflated autosave worker.
 *
 * Rapid edits replace the pending snapshot rather than spawning a database
 * coroutine for every gesture. A single worker writes snapshots in order.
 * flush() waits for the newest submitted snapshot to reach the repository.
 */
internal class EditorAutosaveWorker(
    scope: CoroutineScope,
    private val save: suspend (Map<String, Long>) -> Unit,
    private val debounceMillis: Long = 150L,
    private val onSaveError: (Exception) -> Unit = {}
) {
    private val signal = Channel<Unit>(Channel.CONFLATED)
    private val writeMutex = Mutex()
    private val stateLock = Any()

    private var latestSnapshot: Map<String, Long>? = null
    private var requestedVersion: Long = 0L
    private var persistedVersion: Long = 0L

    init {
        scope.launch {
            for (ignored in signal) {
                delay(debounceMillis)
                try {
                    persistPending()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    onSaveError(error)
                }
            }
        }
    }

    fun submit(snapshot: Map<String, Long>) {
        synchronized(stateLock) {
            latestSnapshot = snapshot.toMap()
            requestedVersion += 1L
        }
        signal.trySend(Unit)
    }

    suspend fun flush() {
        writeMutex.withLock {
            persistPendingLocked()
        }
    }

    private suspend fun persistPending() {
        writeMutex.withLock {
            persistPendingLocked()
        }
    }

    private suspend fun persistPendingLocked() {
        val pending = synchronized(stateLock) {
            if (requestedVersion <= persistedVersion) null
            else latestSnapshot.orEmpty() to requestedVersion
        } ?: return

        save(pending.first)

        synchronized(stateLock) {
            persistedVersion = maxOf(persistedVersion, pending.second)
        }
    }
}
