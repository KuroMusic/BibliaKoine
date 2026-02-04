package com.biblia.koine.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAOs for User-generated content

@Dao
interface HighlightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHighlight(highlight: Highlight)
    
    @Query("""
        SELECT * FROM highlights 
        WHERE bookId = :bookId AND chapter = :chapter AND version = :version
    """)
    suspend fun getHighlightsForChapter(
        bookId: String, 
        chapter: Int, 
        version: String = "default"
    ): List<Highlight>
    
    @Delete
    suspend fun deleteHighlight(highlight: Highlight)


    @Query("SELECT * FROM highlights WHERE bookId = :book AND chapter = :chapter")
    fun getHighlights(book: String, chapter: Int): Flow<List<Highlight>>

    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND chapter = :chapter")
    suspend fun getHighlightsSync(bookId: String, chapter: Int): List<Highlight>

    @Query("SELECT * FROM highlights ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentHighlights(limit: Int): Flow<List<Highlight>>
    
    @Query("SELECT * FROM highlights")
    fun getAllHighlights(): Flow<List<Highlight>>
    

    
    @Delete
    suspend fun delete(highlight: Highlight)
    
    @Query("UPDATE highlights SET color = :color WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun updateVerseColor(bookId: String, chapter: Int, verse: Int, color: String)
    
    @Query("SELECT * FROM highlights WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse LIMIT 1")
    suspend fun getVerseHighlight(bookId: String, chapter: Int, verse: Int): Highlight?
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE bookId = :book AND chapter = :chapter AND verse = :verse")
    fun getNote(book: String, chapter: Int, verse: Int): Flow<Note?>
    
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE bookId = :book AND chapter = :chapter")
    fun getNotes(book: String, chapter: Int): Flow<List<Note>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)
    
    @Delete
    suspend fun delete(note: Note)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>
    
    @Insert
    suspend fun insert(bookmark: Bookmark)
    
    @Delete
    suspend fun delete(bookmark: Bookmark)
    
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE bookId = :book AND chapter = :chapter AND verse = :verse)")
    fun isBookmarked(book: String, chapter: Int, verse: Int): Flow<Boolean>

    @Query("SELECT * FROM bookmarks WHERE bookId = :book AND chapter = :chapter")
    fun getBookmarks(book: String, chapter: Int): Flow<List<Bookmark>>
}



@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id = 1")
    fun getProgress(): Flow<ReadingProgress?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(progress: ReadingProgress)
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getStats(): Flow<UserStats?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStats(stats: UserStats)
    
    @Query("UPDATE user_stats SET chaptersRead = chaptersRead + 1 WHERE id = 1")
    suspend fun incrementChapters()
    
    @Query("UPDATE user_stats SET highlightsCount = highlightsCount + 1 WHERE id = 1")
    suspend fun incrementHighlights()
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSearches(): Flow<List<SearchHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistory)
    
    @Delete
    suspend fun delete(history: SearchHistory)
}


