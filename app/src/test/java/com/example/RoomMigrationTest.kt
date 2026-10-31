package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MandalaDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomMigrationTest {

    @Test
    fun migrationFromV1PreservesRowsAndSeedsLegacyRevisions() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "room_migration_v1_v2_test.db"
        context.deleteDatabase(databaseName)
        val databaseFile: File = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()

        // Build the exact v1 schema exported by Room before opening the v2 DB.
        val legacyDb = SQLiteDatabase.openOrCreateDatabase(databaseFile.absolutePath, null)
        try {
            legacyDb.execSQL(
                """
                CREATE TABLE IF NOT EXISTS mandala_progress (
                    mandalaId TEXT NOT NULL,
                    coloredRegionsJson TEXT NOT NULL,
                    percentCompleted REAL NOT NULL,
                    isFavorite INTEGER NOT NULL,
                    isCompleted INTEGER NOT NULL,
                    lastEditedTimestamp INTEGER NOT NULL,
                    PRIMARY KEY(mandalaId)
                )
                """.trimIndent()
            )
            legacyDb.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            legacyDb.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '0685a95cf4d7a0b632c40a2a0a07f131')"
            )
            legacyDb.execSQL(
                """
                INSERT INTO mandala_progress
                    (mandalaId, coloredRegionsJson, percentCompleted, isFavorite, isCompleted, lastEditedTimestamp)
                VALUES
                    ('legacy_artwork', 'petal_0:4278190335', 0.25, 1, 0, 123456789)
                """.trimIndent()
            )
            legacyDb.version = 1
        } finally {
            legacyDb.close()
        }

        val migratedDb = Room.databaseBuilder(context, MandalaDatabase::class.java, databaseName)
            .addMigrations(MandalaDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            val restored = migratedDb.mandalaDao().getProgressOnce("legacy_artwork")
            assertNotNull("Existing artwork row must survive migration", restored)
            restored!!

            assertEquals("petal_0:4278190335", restored.coloredRegionsJson)
            assertEquals(0.25f, restored.percentCompleted, 0.0001f)
            assertEquals(true, restored.isFavorite)
            assertFalse(restored.isCompleted)
            assertEquals(123456789L, restored.lastEditedTimestamp)

            assertEquals(123456789L, restored.progressRevision)
            assertEquals("legacy", restored.progressActor)
            assertEquals(123456789L, restored.favoriteRevision)
            assertEquals("legacy", restored.favoriteActor)
            assertFalse(restored.isDeleted)
            assertEquals(0L, restored.deletionRevision)
            assertEquals("legacy", restored.deletionActor)
        } finally {
            migratedDb.close()
            context.deleteDatabase(databaseName)
        }
    }
}
