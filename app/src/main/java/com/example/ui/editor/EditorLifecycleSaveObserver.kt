package com.example.ui.editor

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Connects the host lifecycle's ON_STOP event to the editor's ViewModel-owned flush.
 */
internal class EditorLifecycleSaveObserver(
    lifecycle: Lifecycle,
    private val onStop: () -> Unit
) : AutoCloseable {
    private val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) onStop()
    }

    init {
        lifecycle.addObserver(observer)
    }

    override fun close() {
        lifecycle.removeObserver(observer)
    }
}
