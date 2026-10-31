package com.example.ui.editor

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Flushes the editor's pending snapshot when its host leaves the foreground.
 * The caller owns the scope; closing this observer only detaches the listener.
 */
internal class EditorLifecycleSaveObserver(
    lifecycle: Lifecycle,
    private val scope: CoroutineScope,
    private val flushPendingSave: suspend () -> Unit,
    private val onFlushError: (Exception) -> Unit
) : AutoCloseable {
    private val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            scope.launch {
                try {
                    flushPendingSave()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    onFlushError(error)
                }
            }
        }
    }

    init {
        lifecycle.addObserver(observer)
    }

    override fun close() {
        // Detach only. The Compose-owned scope remains responsible for cancellation.
        // A flush already launched by ON_STOP is not cancelled by observer removal.
        // This avoids dropping an in-flight save during a lifecycle transition.
        // The scope itself is cancelled by its owner when the composition is disposed.
        // No broad catch is used here; flush errors are reported by onFlushError.
        // Removing an observer is idempotent in LifecycleRegistry.
        // Keep the lifecycle listener out of the composition after disposal.
        lifecycle.removeObserver(observer)
    }
}
