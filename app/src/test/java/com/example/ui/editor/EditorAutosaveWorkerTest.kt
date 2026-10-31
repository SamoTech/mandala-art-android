package com.example.ui.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorAutosaveWorkerTest {
    @Test
    fun rapidSubmissionsAreConflatedToTheLatestSnapshot() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        val writes = mutableListOf<Map<String, Long>>()
        val worker = EditorAutosaveWorker(
            scope = scope,
            save = { writes += it },
            debounceMillis = 100L
        )
        try {
            runCurrent()
            worker.submit(mapOf("region" to 1L))
            runCurrent()
            advanceTimeBy(40L)
            worker.submit(mapOf("region" to 2L))
            worker.submit(mapOf("region" to 3L))
            advanceTimeBy(60L)
            runCurrent()

            assertEquals(listOf(mapOf("region" to 3L)), writes)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun flushPersistsLatestSnapshotWithoutWaitingForDebounce() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        val writes = mutableListOf<Map<String, Long>>()
        val worker = EditorAutosaveWorker(
            scope = scope,
            save = { writes += it },
            debounceMillis = 10_000L
        )
        try {
            runCurrent()
            worker.submit(mapOf("first" to 1L))
            worker.submit(mapOf("latest" to 9L))

            worker.flush()

            assertEquals(listOf(mapOf("latest" to 9L)), writes)
            advanceTimeBy(10_000L)
            runCurrent()
            assertEquals("A queued wake-up must not write an already persisted snapshot twice", 1, writes.size)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun failedBackgroundWriteCanBeRetriedByExplicitFlush() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        val writes = mutableListOf<Map<String, Long>>()
        var shouldFail = true
        var backgroundErrors = 0
        val worker = EditorAutosaveWorker(
            scope = scope,
            save = {
                writes += it
                if (shouldFail) error("temporary database failure")
            },
            debounceMillis = 10L,
            onSaveError = { backgroundErrors++ }
        )
        try {
            runCurrent()
            worker.submit(mapOf("region" to 7L))
            advanceTimeBy(10L)
            runCurrent()
            assertEquals(1, backgroundErrors)
            assertEquals(1, writes.size)

            shouldFail = false
            worker.flush()
            assertEquals(2, writes.size)
            assertTrue(writes.all { it == mapOf("region" to 7L) })
        } finally {
            scope.cancel()
        }
    }
}
