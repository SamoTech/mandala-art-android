package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MandalaProgressEntity::class], version = 4, exportSchema = true)
abstract class MandalaDatabase : RoomDatabase() {
    abstract fun mandalaDao(): MandalaDao

    companion object {
        /**
         * Adds independent logical revisions for progress and favorites plus a
         * deletion tombstone. Existing rows receive the same legacy revision
         * for both mutable fields, preserving their pre-migration ordering.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN progressRevision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN progressActor TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN favoriteRevision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN favoriteActor TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN deletionRevision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN deletionActor TEXT NOT NULL DEFAULT 'legacy'")

                // v1 stored only one wall-clock timestamp for all fields. Seed
                // both revisions from it so neither field is silently reset.
                db.execSQL(
                    """
                    UPDATE mandala_progress
                    SET progressRevision = CASE WHEN lastEditedTimestamp > 0 THEN lastEditedTimestamp ELSE 0 END,
                        favoriteRevision = CASE WHEN lastEditedTimestamp > 0 THEN lastEditedTimestamp ELSE 0 END
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN restoredDeletionRevision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN restoredDeletionActor TEXT NOT NULL DEFAULT 'legacy'")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN restorationEventRevision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE mandala_progress ADD COLUMN restorationEventActor TEXT NOT NULL DEFAULT 'legacy'")
            }
        }


        /**
         * Rebuilds the progress table with a composite key so identical
         * mandala IDs can exist independently for anonymous and Firebase users.
         * All pre-existing v3 rows remain in the anonymous partition; assigning
         * them to an account automatically would risk cross-account disclosure.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mandala_progress_new (
                        mandalaId TEXT NOT NULL,
                        ownerUid TEXT NOT NULL DEFAULT '',
                        coloredRegionsJson TEXT NOT NULL,
                        percentCompleted REAL NOT NULL,
                        isFavorite INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        lastEditedTimestamp INTEGER NOT NULL,
                        progressRevision INTEGER NOT NULL DEFAULT 0,
                        progressActor TEXT NOT NULL DEFAULT 'legacy',
                        favoriteRevision INTEGER NOT NULL DEFAULT 0,
                        favoriteActor TEXT NOT NULL DEFAULT 'legacy',
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletionRevision INTEGER NOT NULL DEFAULT 0,
                        deletionActor TEXT NOT NULL DEFAULT 'legacy',
                        restoredDeletionRevision INTEGER NOT NULL DEFAULT 0,
                        restoredDeletionActor TEXT NOT NULL DEFAULT 'legacy',
                        restorationEventRevision INTEGER NOT NULL DEFAULT 0,
                        restorationEventActor TEXT NOT NULL DEFAULT 'legacy',
                        PRIMARY KEY(mandalaId, ownerUid)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO mandala_progress_new (
                        mandalaId, ownerUid, coloredRegionsJson, percentCompleted,
                        isFavorite, isCompleted, lastEditedTimestamp, progressRevision,
                        progressActor, favoriteRevision, favoriteActor, isDeleted,
                        deletionRevision, deletionActor, restoredDeletionRevision,
                        restoredDeletionActor, restorationEventRevision, restorationEventActor
                    )
                    SELECT mandalaId, '', coloredRegionsJson, percentCompleted,
                        isFavorite, isCompleted, lastEditedTimestamp, progressRevision,
                        progressActor, favoriteRevision, favoriteActor, isDeleted,
                        deletionRevision, deletionActor, restoredDeletionRevision,
                        restoredDeletionActor, restorationEventRevision, restorationEventActor
                    FROM mandala_progress
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE mandala_progress")
                db.execSQL("ALTER TABLE mandala_progress_new RENAME TO mandala_progress")
            }
        }

        @Volatile
        private var INSTANCE: MandalaDatabase? = null

        fun getInstance(context: Context): MandalaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MandalaDatabase::class.java,
                    "mandala_art_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
