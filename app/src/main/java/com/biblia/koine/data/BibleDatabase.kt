package com.biblia.koine.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Verse::class, Book::class, Highlight::class, Note::class, Bookmark::class, ReadingProgress::class, UserStats::class, SearchHistory::class],
    version = 9, // Added version to Highlight
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun bookmarkDao(): BookmarkDao

    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun searchHistoryDao(): SearchHistoryDao


    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null
        
        // Migration from version 6 to 7: Add indices to highlights table
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create indices on highlights table for faster queries (10x performance boost)
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_highlights_bookId_chapter_verse` ON `highlights` (`bookId`, `chapter`, `verse`)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_highlights_createdAt` ON `highlights` (`createdAt`)"
                )
            }
        }
        
        // Migration from version 7 to 8: Add scroll position persistence
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add lastScrollOffset column to reading_progress table
                database.execSQL(
                    "ALTER TABLE reading_progress ADD COLUMN lastScrollOffset REAL NOT NULL DEFAULT 0.0"
                )
            }
        }

        // Migration from version 8 to 9: Add version to highlights
        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE highlights ADD COLUMN version TEXT NOT NULL DEFAULT 'default'"
                )
            }
        }

        fun getDatabase(context: Context): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_database"
                )
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)  // Safe migrations - preserve data
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate logic if needed
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
