package com.example.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MandalaDatabase
import com.example.data.MandalaRepository
import com.example.util.AppSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditorAccountSwitchTest {

    @Test
    fun pendingEditStaysWithOriginalOwnerWhenAccountSwitchCreatesNewEditorViewModel() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(database.mandalaDao(), "account-switch-test-device")
        val settings = AppSettings(context)
        val viewModelStore = ViewModelStore()

        try {
            repository.setOwnerUid("account-a")
            val firstEditor = ViewModelProvider(
                viewModelStore,
                EditorViewModel.Factory(
                    mandalaId = "lotus_bloom",
                    repository = repository,
                    appSettings = settings,
                    ownerUid = repository.currentOwnerUid
                )
            ).get("account-a:lotus_bloom", EditorViewModel::class.java)

            // Submit an edit but do not wait for the worker's debounce interval.
            firstEditor.onRegionTapped("petal_0", context)

            // This mirrors MainActivity's account observer and UID-qualified ViewModel key.
            repository.setOwnerUid("account-b")
            val secondEditor = ViewModelProvider(
                viewModelStore,
                EditorViewModel.Factory(
                    mandalaId = "lotus_bloom",
                    repository = repository,
                    appSettings = settings,
                    ownerUid = repository.currentOwnerUid
                )
            ).get("account-b:lotus_bloom", EditorViewModel::class.java)

            assertNotSame("A different account must receive a different editor ViewModel", firstEditor, secondEditor)

            // The previous Activity-scoped ViewModel remains addressable in the same store.
            // Its pending save must remain bound to account A after account B becomes active.
            firstEditor.flushPendingSave()

            val accountAProgress = database.mandalaDao().getProgressOnce("lotus_bloom", "account-a")
            val accountBProgress = database.mandalaDao().getProgressOnce("lotus_bloom", "account-b")

            assertNotNull("The pending edit must be persisted for its original owner", accountAProgress)
            assertTrue("The persisted snapshot must contain the edit", accountAProgress!!.coloredRegionsJson.isNotBlank())
            assertNull("The pending edit must never be written to the newly active account", accountBProgress)
        } finally {
            viewModelStore.clear()
            database.close()
        }
    }
    @Test
    fun accountSwitchDuringInFlightSaveKeepsWriteBoundToOriginalOwner() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(database.mandalaDao(), "in-flight-switch-test-device")
        val settings = AppSettings(context)
        val viewModelStore = ViewModelStore()
        val saveStarted = CompletableDeferred<Unit>()
        val releaseSave = CompletableDeferred<Unit>()
        val observedOwnerUids = mutableListOf<String>()

        try {
            repository.setOwnerUid("account-a")
            val factory = EditorViewModel.Factory(
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
                        totalRegions = EditorViewModel(
                            mandalaId = "lotus_bloom",
                            repository = repository,
                            appSettings = settings,
                            ownerUid = ownerUid
                        ).design.regionCount,
                        ownerUid = ownerUid
                    )
                }
            )
            val editor = ViewModelProvider(viewModelStore, factory)
                .get("account-a:lotus_bloom", EditorViewModel::class.java)

            editor.onRegionTapped("petal_0", context)
            val flush = async { editor.flushPendingSave() }
            saveStarted.await()

            repository.setOwnerUid("account-b")
            releaseSave.complete(Unit)
            flush.await()

            assertTrue("Every in-flight write must retain the editor's original UID", observedOwnerUids.all { it == "account-a" })
            assertNotNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-a"))
            assertNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-b"))
        } finally {
            releaseSave.complete(Unit)
            viewModelStore.clear()
            database.close()
        }
    }

    @Test
    fun failedSaveRetryAfterAccountSwitchStillUsesOriginalOwnerUid() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, MandalaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = MandalaRepository(database.mandalaDao(), "failed-switch-test-device")
        val settings = AppSettings(context)
        val viewModelStore = ViewModelStore()
        val observedOwnerUids = mutableListOf<String>()
        var failFirstAttempt = true

        try {
            repository.setOwnerUid("account-a")
            val factory = EditorViewModel.Factory(
                mandalaId = "lotus_bloom",
                repository = repository,
                appSettings = settings,
                ownerUid = "account-a",
                persistProgress = { ownerUid, snapshot ->
                    observedOwnerUids += ownerUid
                    if (failFirstAttempt) {
                        failFirstAttempt = false
                        throw IOException("temporary persistence failure")
                    }
                    repository.saveProgress(
                        mandalaId = "lotus_bloom",
                        coloredRegions = snapshot,
                        totalRegions = 12,
                        ownerUid = ownerUid
                    )
                }
            )
            val editor = ViewModelProvider(viewModelStore, factory)
                .get("account-a:lotus_bloom", EditorViewModel::class.java)

            editor.onRegionTapped("petal_0", context)
            var failedAsExpected = false
            try {
                editor.flushPendingSave()
            } catch (_: IOException) {
                failedAsExpected = true
            }
            assertTrue("The first persistence attempt must fail in this regression test", failedAsExpected)

            repository.setOwnerUid("account-b")
            editor.flushPendingSave()

            assertEquals(listOf("account-a", "account-a"), observedOwnerUids)
            assertNotNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-a"))
            assertNull(database.mandalaDao().getProgressOnce("lotus_bloom", "account-b"))
        } finally {
            viewModelStore.clear()
            database.close()
        }
    }

}
