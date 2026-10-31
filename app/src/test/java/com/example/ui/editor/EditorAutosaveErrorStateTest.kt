package com.example.ui.editor

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MandalaDatabase
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.util.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditorAutosaveErrorStateTest {

    @Test
    fun backgroundDebounceFailureIsVisibleAndSuccessfulRetryClearsError() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(database.mandalaDao(), "autosave-error-test-device")
        val settings = AppSettings(context)
        val viewModelStore = ViewModelStore()
        var shouldFail = true
        var attempts = 0

        try {
            repository.setOwnerUid("account-a")
            val editor = ViewModelProvider(
                viewModelStore,
                EditorViewModel.Factory(
                    mandalaId = "lotus_bloom",
                    repository = repository,
                    appSettings = settings,
                    ownerUid = "account-a",
                    persistProgress = { ownerUid, snapshot ->
                        attempts++
                        if (shouldFail) throw IOException("temporary persistence failure")
                        repository.saveProgress(
                            mandalaId = "lotus_bloom",
                            coloredRegions = snapshot,
                            totalRegions = MandalaLibrary.getDesignById("lotus_bloom").regionCount,
                            ownerUid = ownerUid
                        )
                    }
                )
            ).get("account-a:lotus_bloom", EditorViewModel::class.java)

            runCurrent()
            editor.onRegionTapped("petal_0", context)
            advanceTimeBy(150L)
            runCurrent()

            assertTrue("A failed debounce write must publish an in-app error state", editor.hasAutosaveError.value)
            assertEquals(1L, editor.autosaveErrorVersion.value)
            assertEquals(1, attempts)

            shouldFail = false
            editor.flushPendingSave()

            assertFalse("A successful retry must clear the autosave error state", editor.hasAutosaveError.value)
            assertEquals(2, attempts)
            assertNotNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-a"))
        } finally {
            viewModelStore.clear()
            database.close()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun onStopFailureAndFailedRetryRemainVisibleUntilSnapshotIsSaved() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(database.mandalaDao(), "on-stop-autosave-error-device")
        val settings = AppSettings(context)
        val viewModelStore = ViewModelStore()
        var attempts = 0
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        val editor: EditorViewModel
        val observer: EditorLifecycleSaveObserver

        try {
            repository.setOwnerUid("account-a")
            editor = ViewModelProvider(
                viewModelStore,
                EditorViewModel.Factory(
                    mandalaId = "lotus_bloom",
                    repository = repository,
                    appSettings = settings,
                    ownerUid = "account-a",
                    persistProgress = { ownerUid, snapshot ->
                        attempts++
                        if (attempts <= 2) throw IOException("temporary persistence failure")
                        repository.saveProgress(
                            mandalaId = "lotus_bloom",
                            coloredRegions = snapshot,
                            totalRegions = MandalaLibrary.getDesignById("lotus_bloom").regionCount,
                            ownerUid = ownerUid
                        )
                    }
                )
            ).get("account-a:lotus_bloom", EditorViewModel::class.java)
            observer = EditorLifecycleSaveObserver(lifecycleOwner.lifecycle) {
                editor.flushPendingSaveOnLifecycleStop { /* ViewModel retains the actionable error state. */ }
            }

            runCurrent()
            editor.onRegionTapped("petal_0", context)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            runCurrent()

            assertTrue("An ON_STOP flush failure must remain visible after returning to the editor", editor.hasAutosaveError.value)
            assertEquals(1L, editor.autosaveErrorVersion.value)

            var retryFailed = false
            try {
                editor.flushPendingSave()
            } catch (_: IOException) {
                retryFailed = true
            }
            assertTrue("The first explicit retry is expected to fail", retryFailed)
            assertTrue("A failed retry must not clear the error state", editor.hasAutosaveError.value)
            assertEquals(2L, editor.autosaveErrorVersion.value)

            editor.flushPendingSave()
            assertFalse("The error clears only after the pending snapshot is saved", editor.hasAutosaveError.value)
            assertNotNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-a"))
        } finally {
            if (::observer.isInitialized) observer.close()
            viewModelStore.clear()
            database.close()
            Dispatchers.resetMain()
        }
    }
}
