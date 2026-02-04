package com.biblia.koine.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BibleEntity::class, StrongDefinition::class, Pericope::class, VersionEntity::class], version = 22, exportSchema = false)
abstract class BibliaDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibliaDatabase? = null

        fun getDatabase(context: Context): BibliaDatabase {
            return INSTANCE ?: synchronized(this) {
                // Use applicationContext to prevent leaks
                val appContext = context.applicationContext
                
                val instance = Room.databaseBuilder(
                    appContext,
                    BibliaDatabase::class.java,
                    "BibliaKoine.db"
                )
                // Simplify: Tell Room to copy from assets only if the file doesn't exist
                .createFromAsset("databases/BibliaKoine.db")
                // EXTREME OPTIMIZATION: WAL mode allows concurrent reads and faster writes
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // PRAGMA synchronous = NORMAL is faster and safe in WAL mode
                        db.execSQL("PRAGMA synchronous = NORMAL")
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
