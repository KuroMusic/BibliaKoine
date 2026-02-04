package com.biblia.koine.data.repository

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import com.biblia.koine.data.BibleBook
import com.biblia.koine.data.BibleBooksMetadata
import com.biblia.koine.data.BibleDatabase
import com.biblia.koine.data.Bookmark
import com.biblia.koine.data.Highlight
import com.biblia.koine.data.Note
import com.biblia.koine.data.*
import com.biblia.koine.data.room.BibliaDatabase
import com.biblia.koine.data.room.BibleVerse
import com.biblia.koine.data.room.StrongDefinition
import com.biblia.koine.data.room.Pericope
import com.biblia.koine.viewmodel.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

data class ChapterContent(
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verses: List<BibleVerse>,
    val version: String
)

data class VerseWithPericope(
    val verse: BibleVerse,
    val pericope: Pericope? = null
)
class BibleRepository(context: Context) {
    // Room Database for Bible Text
    // Lazy initialization ensures access triggers creation/decompression
    // Since loadInitialData runs on IO, this remains "Anti-Freeze" safe.
    private val bibleDb by lazy { BibliaDatabase.getDatabase(context) }
    private val bibleDao by lazy { bibleDb.bibleDao() }

    // Room Database for User Data
    private val userDataDb = BibleDatabase.getDatabase(context)
    
    // User Prefs for default version logic
    private val userPreferences = com.biblia.koine.data.prefs.UserPreferences(context)

    init {
        // ASYNC INITIALIZATION (Decompression UI Support)
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
             // 1. Force Init (Accessing dao triggers lazy init -> getDatabase -> decompression)
             try {
                bibleDao.getAvailableVersions() 
             } catch (e: Exception) {
                e.printStackTrace()
             }
             
             // 2. Set Default Version
             initializeDefaultVersion()
             
             // 3. Signal Readiness
             com.biblia.koine.data.DatabaseModule.setDatabaseReady(true)
        }
    }
    
    private suspend fun initializeDefaultVersion() {
        // Check if version is already set
        val currentVersion = userPreferences.getLastVersion()
        if (currentVersion.isNullOrBlank()) {
            // First run! Find a Spanish version
            val versions = bibleDao.getAvailableVersions()
            val rvr = versions.find { it == "RVR1960" }
            val lbla = versions.find { it == "LBLA" }
            
            val default = rvr ?: lbla ?: "RVR1960" // Fallback
            
            userPreferences.saveLastVersion(default)
        }
    }
    
    private var cachedBooks: List<BibleBook> = emptyList()
    private var cachedBooksVersion: String = ""

    // --- BIBLE DATA (Room) ---
    
    // Legacy support via simple query
    // Legacy support via simple query
    suspend fun getVersions(): List<String> = try {
        // Query distinct versions from DB via DAO (using join)
        // Since we don't have a direct "getVersions" in DAO yet that returns strings, 
        // implies we should add one or rely on the fact we have fixed versions.
        // But to support "Language Filter" we should query the `versiones` table.
        // DAO needs a method for this.
        // For now, let's add `getAvailableVersions` to DAO.
        bibleDao.getAvailableVersions()
    } catch (e: Exception) {
        listOf("RVR1960", "NVI", "LBLA") 
    } 

    suspend fun getBooks(version: String): List<BibleBook> = withContext(Dispatchers.IO) {
        if (cachedBooks.isNotEmpty() && cachedBooksVersion == version) {
            return@withContext cachedBooks
        }

        val bookNums = bibleDao.getBooks(version)
        val books = mutableListOf<BibleBook>()
        
        for (num in bookNums) {
            val id = BibleBooksMetadata.getId(num)
            if (id != null) {
                val nameSpan = BibleBooksMetadata.getName(id)
                val maxChapter = 50 
                val chapters = bibleDao.getChapters(num, version).size
                
                books.add(BibleBook(
                    id = id,
                    name = nameSpan,
                    testament = if (num < 40) Testament.OLD else Testament.NEW,
                    chapters = if (chapters > 0) chapters else 1,
                    bookNumber = num
                ))
            }
        }
        cachedBooks = books
        cachedBooksVersion = version
        cachedBooks
    }

    // New Reactive Methods
    fun getChapterVerses(bookId: String, chapter: Int, version: String): Flow<List<BibleVerse>> {
        val bookNum = BibleBooksMetadata.getNumber(bookId)
        return bibleDao.getChapterVerses(bookNum, chapter, version)
    }

    fun getPericopes(bookId: String, chapter: Int): Flow<List<Pericope>> {
        val bookNum = BibleBooksMetadata.getNumber(bookId)
        return bibleDao.getPericopesForChapter(bookNum, chapter)
    }

    private fun getVersionId(version: String): Int {
        return when (version) {
            "RVR1960" -> 1
            "NVI" -> 2
            "LBLA" -> 3
            else -> 1
        }
    }

    // --- JOINED QUERY (Verses + Pericopes) ---
    // Solves the "Missing Titles" issue by packaging them together
    
    fun getChapterWithPericopes(version: String, bookId: String, chapter: Int): Flow<List<VerseWithPericope>> = kotlinx.coroutines.flow.flow {
        val bookNum = BibleBooksMetadata.getNumber(bookId)
        
        // 1. Fetch Verses
        val verses = bibleDao.getChapterVersesSync(bookNum, chapter, version)
        
        // 2. Fetch Pericopes (No version filter as requested)
        val pericopes = bibleDao.getPericopesForChapterSync(bookNum, chapter)
        
        // 3. JOIN
        val combined = verses.map { v ->
            VerseWithPericope(
                verse = v,
                pericope = pericopes.find { it.startVerse == v.verse }
            )
        }
        emit(combined)
    }.flowOn(Dispatchers.IO) // Flow is already on IO, but internal suspend calls should be safe too via Room

    // --- STRICT IO WRAPPERS (EXTREME OPTIMIZATION) ---

    suspend fun saveHighlight(highlight: Highlight) = withContext(Dispatchers.IO) {
        userDataDb.highlightDao().saveHighlight(highlight)
    }

    suspend fun getHighlightsForChapter(bookId: String, chapter: Int, version: String = "default"): List<Highlight> = withContext(Dispatchers.IO) {
        userDataDb.highlightDao().getHighlightsForChapter(bookId, chapter, version)
    }

    suspend fun updateHighlight(bookId: String, chapter: Int, verse: Int, color: String) = withContext(Dispatchers.IO) {
        val highlight = Highlight(
            bookId = bookId,
            chapter = chapter,
            verse = verse,
            color = color
        )
        saveHighlight(highlight)
    }
    
    suspend fun updateVerseColor(bookId: String, chapter: Int, verse: Int, color: String) = withContext(Dispatchers.IO) {
        userDataDb.highlightDao().updateVerseColor(bookId, chapter, verse, color)
    }
    
    suspend fun getVerseHighlight(bookId: String, chapter: Int, verse: Int): Highlight? = withContext(Dispatchers.IO) {
        userDataDb.highlightDao().getVerseHighlight(bookId, chapter, verse)
    }
    
    suspend fun removeHighlight(bookId: String, chapter: Int, verse: Int) = withContext(Dispatchers.IO) {
        val highlights = userDataDb.highlightDao().getHighlightsSync(bookId, chapter)
        val highlight = highlights.find { it.verse == verse }
        highlight?.let { userDataDb.highlightDao().delete(it) }
    }
    
    suspend fun saveNote(note: Note) = withContext(Dispatchers.IO) {
        userDataDb.noteDao().insert(note)
    }
    
    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        userDataDb.noteDao().delete(note)
    }
    
    suspend fun addBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        userDataDb.bookmarkDao().insert(bookmark)
    }
    
    suspend fun removeBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        userDataDb.bookmarkDao().delete(bookmark)
    }
    
    suspend fun getHighlightsSync(bookId: String, chapter: Int): List<Highlight> = withContext(Dispatchers.IO) {
        userDataDb.highlightDao().getHighlightsSync(bookId, chapter)
    }
    
    suspend fun getReadingProgressSync(): com.biblia.koine.data.ReadingProgress? = withContext(Dispatchers.IO) {
        userDataDb.readingProgressDao().getProgress().firstOrNull()
    }
    
    suspend fun updateReadingProgress(progress: com.biblia.koine.data.ReadingProgress) = withContext(Dispatchers.IO) {
        userDataDb.readingProgressDao().update(progress)
    }

    suspend fun updateStats(stats: com.biblia.koine.data.UserStats) = withContext(Dispatchers.IO) {
        userDataDb.userStatsDao().updateStats(stats)
    }
    
    suspend fun incrementChaptersRead() = withContext(Dispatchers.IO) {
        userDataDb.userStatsDao().incrementChapters()
    }
    
    suspend fun incrementHighlights() = withContext(Dispatchers.IO) {
        userDataDb.userStatsDao().incrementHighlights()
    }
    
    suspend fun saveSearchToHistory(query: String) = withContext(Dispatchers.IO) {
        userDataDb.searchHistoryDao().insert(com.biblia.koine.data.SearchHistory(query))
    }
    
    suspend fun deleteSearchFromHistory(history: com.biblia.koine.data.SearchHistory) = withContext(Dispatchers.IO) {
        userDataDb.searchHistoryDao().delete(history)
    }
    


    // Database Readiness Check
    init {
        // Start checking immediately in background
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            checkDatabaseReadiness()
        }
    }

    private suspend fun checkDatabaseReadiness() {
        while (true) {
            try {
                // Dummy query to check if DB is open and populated
                val books = getBooks("RVR1960")
                if (books.isNotEmpty()) {
                    DatabaseModule.setDatabaseReady(true)
                    break
                }
            } catch (e: Exception) {
                kotlinx.coroutines.delay(200)
            }
        }
    }

    // Deprecated: Use DatabaseModule.isDatabaseReady
    val isDatabaseReady: Flow<Boolean> = DatabaseModule.isDatabaseReady
    
    fun getStrongDefinition(topic: String): Flow<StrongDefinition?> {
        return bibleDao.getStrongDefinition(topic)
    }

    // Level 1 Cache (Memory)
    private val chapterCache = object : android.util.LruCache<String, ChapterContent>(5) {}

    suspend fun getChapterContent(bookId: String, chapter: Int, version: String): ChapterContent {
        val cacheKey = "${version}_${bookId}_$chapter"
        
        // Check Cache
        chapterCache.get(cacheKey)?.let { return it }
        
        return withContext(Dispatchers.IO) {
            val bookNum = BibleBooksMetadata.getNumber(bookId)
            
            // Get verses from Flow (first emission)
            val verses = bibleDao.getChapterVerses(bookNum, chapter, version).firstOrNull() ?: emptyList()
            
            val bookName = BibleBooksMetadata.getName(bookId)
            val content = ChapterContent(bookId, bookName, chapter, verses, version)
            
            // Save to Cache
            chapterCache.put(cacheKey, content)
            
            content
        }
    }
    
    fun getVersesFlow(bookId: String, chapter: Int, version: String): Flow<List<BibleVerse>> {
        val bookNum = BibleBooksMetadata.getNumber(bookId)
        return bibleDao.getChapterVerses(bookNum, chapter, version)
    }
    
    fun getVerses(bookId: String, chapter: Int, version: String): Flow<List<BibleVerse>> {
        return getChapterVerses(bookId, chapter, version)
    }

    suspend fun search(query: String, version: String): List<BibleVerse> = withContext(Dispatchers.IO) {
        bibleDao.searchBible(query, version)
    }

    fun searchBibleFTS(query: String, version: String): Flow<List<com.biblia.koine.data.room.SearchResult>> = kotlinx.coroutines.flow.flow {
        emit(bibleDao.searchBibleFTS(query, version))
    }

    // Support for direct Int access as requested
    // Support for direct Int access as requested, actively observing version changes
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getChapter(bookId: Int, chapter: Int): Flow<List<BibleVerse>> {
        return userPreferences.prefsFlow
            .map { it.bibleVersion }
            .distinctUntilChanged()
            .flatMapLatest { version ->
                bibleDao.getChapterVerses(bookId, chapter, version)
            }
    }
    
    // YouVersion-style unlimited search
    suspend fun searchVerses(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val bibleResults = bibleDao.searchBible(query, "RVR1960") // Default to RVR1960 for general search
        bibleResults.map { 
            val name = BibleBooksMetadata.getName(BibleBooksMetadata.getId(it.book_num) ?: "")
            SearchResult(
                id = it.id,
                reference = "$name ${it.chapter}:${it.verse}",
                text = it.text,
                bookId = BibleBooksMetadata.getId(it.book_num) ?: "",
                chapter = it.chapter,
                verse = it.verse
            )
        }
    }
    
    // YouVersion-style unlimited search (Returning raw verses for ViewModel to map)
    suspend fun searchUnlimited(query: String, version: String, bookNumbers: IntRange? = null): List<com.biblia.koine.data.room.BibleVerse> = withContext(Dispatchers.IO) {
        // Simple search for now to match new DAO
        bibleDao.searchBible(query, version)
    }
    
    suspend fun getVersesCount(bookId: String, chapter: Int, version: String): List<Int> = withContext(Dispatchers.IO) {
        val bookNum = BibleBooksMetadata.getNumber(bookId)
        bibleDao.getVersesCount(bookNum, chapter, version)
    }

    suspend fun getChapters(bookId: String, version: String): List<Int> = withContext(Dispatchers.IO) {
        val bookNum = BibleBooksMetadata.getNumber(bookId)
        bibleDao.getChapters(bookNum, version)
    }
    
    // --- NON-CONFLICTING ORIGINAL METHODS (Re-incorporated) ---

    suspend fun searchDictionary(query: String): List<com.biblia.koine.data.room.StrongDefinition> = withContext(Dispatchers.IO) {
        try {
            bibleDao.searchDictionary(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getHighlights(bookId: String, chapter: Int): Flow<List<Highlight>> {
        return userDataDb.highlightDao().getHighlights(bookId, chapter)
    }
    
    fun getRecentHighlights(limit: Int): Flow<List<Highlight>> {
        return userDataDb.highlightDao().getRecentHighlights(limit)
    }

    fun getNote(bookId: String, chapter: Int, verse: Int): Flow<Note?> {
        return userDataDb.noteDao().getNote(bookId, chapter, verse)
    }

    fun getAllBookmarks(): Flow<List<Bookmark>> {
        return userDataDb.bookmarkDao().getAllBookmarks()
    }

    fun getAllHighlights(): Flow<List<Highlight>> {
        return userDataDb.highlightDao().getAllHighlights()
    }
    
    fun getBookmarks(bookId: String, chapter: Int): Flow<List<Bookmark>> = userDataDb.bookmarkDao().getBookmarks(bookId, chapter)
    
    fun getNotes(bookId: String, chapter: Int): Flow<List<Note>> = userDataDb.noteDao().getNotes(bookId, chapter)

    fun getAllNotes(): Flow<List<Note>> {
        return userDataDb.noteDao().getAllNotes()
    }
    
    fun isBookmarked(bookId: String, chapter: Int, verse: Int): Flow<Boolean> {
        return userDataDb.bookmarkDao().isBookmarked(bookId, chapter, verse)
    }

    fun getNotesForChapter(bookId: String, chapter: Int): Flow<List<Note>> {
        return userDataDb.noteDao().getNotes(bookId, chapter)
    }

    fun getBookmarksForChapter(bookId: String, chapter: Int): Flow<List<Bookmark>> {
        return userDataDb.bookmarkDao().getBookmarks(bookId, chapter)
    }
    

    
    fun getReadingProgress(): Flow<com.biblia.koine.data.ReadingProgress?> {
        return userDataDb.readingProgressDao().getProgress()
    }
    
    fun getUserStats(): Flow<com.biblia.koine.data.UserStats?> {
        return userDataDb.userStatsDao().getStats()
    }
    
    fun getSearchHistory(): Flow<List<com.biblia.koine.data.SearchHistory>> {
        return userDataDb.searchHistoryDao().getRecentSearches()
    }
    

    
    suspend fun getBook(bookId: String, version: String = "RVR1960"): BibleBook? {
        return getBooks(version).find { it.id == bookId }
    }

    suspend fun optimizeDatabase() = withContext(Dispatchers.IO) {
        try {
            bibleDb.openHelper.writableDatabase.apply {
                execSQL("CREATE INDEX IF NOT EXISTS idx_search_text ON bible_content(text)")
                execSQL("CREATE INDEX IF NOT EXISTS idx_book_chapter ON bible_content(book_num, chapter)")
                execSQL("VACUUM")
                execSQL("ANALYZE")
            }
            
            userDataDb.openHelper.writableDatabase.apply {
                execSQL("CREATE INDEX IF NOT EXISTS highlights_idx ON highlights(bookId, chapter, verse)")
                execSQL("VACUUM")
                execSQL("ANALYZE")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
