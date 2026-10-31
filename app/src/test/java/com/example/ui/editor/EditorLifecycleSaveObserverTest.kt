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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditorLifecycleSaveObserverTest {

    @Test
    fun onStopFlushCompletesAndKeepsPendingSnapshotInOriginalAccountPartition() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(database.mandalaDao(), "on-stop-switch-test-device")
        val settings = AppSettings(context)
        val viewModelStore = ViewModelStore()
        val saveStarted = CompletableDeferred<Unit>()
        val releaseSave = CompletableDeferred<Unit>()
        val observedOwnerUids = mutableListOf<String>()
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        var observer: EditorLifecycleSaveObserver? = null

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
                        observedOwnerUids += ownerUid
                        saveStarted.complete(Unit)
                        releaseSave.await()
                        repository.saveProgress(
                            mandalaId = "lotus_bloom",
                            coloredRegions = snapshot,
                            totalRegions = MandalaLibrary.getDesignById("lotus_bloom").regionCount,
                            ownerUid = ownerUid
                        )
                    }
                )
            ).get("account-a:lotus_bloom", EditorViewModel::class.java)

            editor.onRegionTapped("petal_0", context)
            observer = EditorLifecycleSaveObserver(
                lifecycle = lifecycleOwner.lifecycle,
                onStop = {
                    editor.flushPendingSaveOnLifecycleStop {
                        throw AssertionError("ON_STOP flush failed", it)
                    }
                }
            )

            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            runCurrent()

            assertTrue("ON_STOP must start flushing the pending snapshot", saveStarted.isCompleted)
            repository.setOwnerUid("account-b")
            releaseSave.complete(Unit)
            advanceUntilIdle()

            assertEquals(listOf("account-a"), observedOwnerUids)
            assertNotNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-a"))
            assertNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-b"))
        } finally {
            observer?.close()
            releaseSave.complete(Unit)
            viewModelStore.clear()
            database.close()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun closingObserverDetachesItFromFurtherLifecycleEvents() = runTest {
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        var flushCount = 0
        val observer = EditorLifecycleSaveObserver(
            lifecycle = lifecycleOwner.lifecycle,
            onStop = { flushCount++ }
        )

        observer.close()
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        runCurrent()

        assertEquals(0, flushCount)
    }
}
