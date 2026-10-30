package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MandalaProgressEntity::class], version = 1, exportSchema = true)
abstract class MandalaDatabase : RoomDatabase() {
    abstract fun mandalaDao(): MandalaDao

    companion object {
        @Volatile
        private var INSTANCE: MandalaDatabase? = null

        fun getInstance(context: Context): MandalaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MandalaDatabase::class.java,
                    "mandala_art_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
