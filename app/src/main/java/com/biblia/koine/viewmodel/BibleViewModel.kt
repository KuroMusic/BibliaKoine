package com.biblia.koine.viewmodel

import kotlinx.coroutines.flow.firstOrNull
import android.app.Application
import java.util.Calendar
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biblia.koine.data.*
import com.biblia.koine.data.BibleBook
import com.biblia.koine.data.Testament
import com.biblia.koine.data.bibleBooks
import com.biblia.koine.data.prefs.UserPreferences
import com.biblia.koine.data.repository.BibleRepository
import com.biblia.koine.data.room.BibleVerse
import com.biblia.koine.data.room.StrongDefinition
import com.biblia.koine.data.room.Pericope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.util.LruCache

enum class SearchTestamentFilter {
    ALL, OLD_TESTAMENT, NEW_TESTAMENT
}

data class DisplayVerse(
    val id: Int,
    val number: Int,
    val text: String,
    val highlightColor: Color? = null,
    val isWordsOfChrist: Boolean = false,
    val bookId: String,
    val chapter: Int,
    val bookName: String,
    val heading: String? = null,
    val hasNote: Boolean = false,
    val isBookmarked: Boolean = false,
    val strongs: String? = null,
    val isRed: Boolean = false
) {
    val reference: String get() = "$bookName $chapter:$number"
}

data class SearchResult(
    val id: Int,
    val reference: String,
    val text: String,
    val bookId: String,
    val chapter: Int,
    val verse: Int
)



fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) { Color.Transparent }

data class BibleUiState(
    val isLoading: Boolean = false,
    val verses: List<DisplayVerse> = emptyList(),
    val currentVersion: String = "RVR1960",
    val error: String? = null,
    val scrollToVerse: Pair<Int, Int>? = null
)


@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BibleRepository(application)
    val userPreferences = UserPreferences(application)

    fun optimizeDatabase() {
        viewModelScope.launch {
            repository.optimizeDatabase()
        }
    }

    // --- NAVIGATION STATE ---
    // No hardcoded defaults - will be loaded from cache/repository
    
    private val _currentBookId = MutableStateFlow("")
    val currentBookId: StateFlow<String> = _currentBookId.asStateFlow()
    
    // Kept for compatibility with some UI, syncing with ID
    private val _currentBook = MutableStateFlow<BibleBook?>(null)
    val currentBook: StateFlow<BibleBook?> = _currentBook.asStateFlow()

    private val _currentChapter = MutableStateFlow(1)
    val currentChapter: StateFlow<Int> = _currentChapter.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // --- REACTIVE UI TOGGLES (Linked to DataStore) ---
    val userPrefs = userPreferences.prefsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserPreferences.Prefs()
    )

    val currentVersion = userPrefs.map { it.bibleVersion }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "RVR1960")
    val showRed = userPrefs.map { it.showRedLetters }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val showStrong = userPrefs.map { it.enableStrongs }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val showTitles = userPrefs.map { it.showSectionTitles }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val showVerseNumbers = userPrefs.map { it.showVerseNumbers }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _availableVersions = MutableStateFlow<List<String>>(listOf("RVR1960", "NVI", "LBLA"))
    val availableVersions: StateFlow<List<String>> = _availableVersions.asStateFlow()

    private val _currentBookName = MutableStateFlow("")
    val currentBookName: StateFlow<String> = _currentBookName.asStateFlow()

    // --- DATA ---
    private val _verses = MutableStateFlow<List<DisplayVerse>>(emptyList())
    val verses: StateFlow<List<DisplayVerse>> = _verses.asStateFlow()
    
    private val _books = MutableStateFlow<List<BibleBook>>(emptyList())
    val books: StateFlow<List<BibleBook>> = _books.asStateFlow()
    val allBooks = _books // Alias
    
    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _highlights = MutableStateFlow<List<Highlight>>(emptyList())
    val highlights: StateFlow<List<Highlight>> = _highlights.asStateFlow()
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // --- PAGING SUPPORT ---
    // Moved here to ensure _highlights, _notes, etc are defined
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getChapterFlow(index: Int): Flow<List<DisplayVerse>> {
        val (bookId, chapter) = BibleBooksMetadata.getChapterFromIndex(index)
        
        return combine(currentVersion, showRed, showStrong, showTitles, showVerseNumbers) { version, red, strong, titles, numbers ->
            version to listOf(red, strong, titles, numbers)
        }.distinctUntilChanged()
        .flatMapLatest { (version, settings) ->
            val redVisible = settings[0] as Boolean
            val strongVisible = settings[1] as Boolean
            val titlesVisible = settings[2] as Boolean
            // numbersVisible not strictly needed here for data mapping but good to have
            
            val bookName = BibleBooksMetadata.getName(bookId)
            
            combine(
                repository.getChapterWithPericopes(version, bookId, chapter),
                repository.getHighlights(bookId, chapter),
                repository.getBookmarks(bookId, chapter),
                repository.getNotes(bookId, chapter)
            ) { versesWithPericopes, highlights, bookmarks, notes ->
                versesWithPericopes.map { wrapper ->
                    val v = wrapper.verse
                    val pericope = wrapper.pericope
                    
                    val highlight = highlights.find { it.verse == v.verse }
                    val hasNote = notes.any { it.verse == v.verse }
                    val isBookmarked = bookmarks.any { it.verse == v.verse }
                    
                    // Use the pericope from the joined wrapper
                    val heading = if (titlesVisible) pericope?.title else null
                    
                    DisplayVerse(
                        id = v.id,
                        number = v.verse,
                        text = v.text,
                        bookId = bookId,
                        chapter = chapter,
                        bookName = bookName,
                        highlightColor = highlight?.color?.toComposeColor(),
                        hasNote = hasNote,
                        isBookmarked = isBookmarked,
                        heading = heading,
                        isWordsOfChrist = (v.is_red ?: 0) == 1,
                        strongs = if (strongVisible) v.strongs else null,
                        isRed = redVisible && (v.is_red ?: 0) == 1
                    )
                }
            }.flowOn(Dispatchers.Default) // PROCESS ON DEFAULT
        }.flowOn(Dispatchers.IO)
    }

    // New helper for BibleReaderScreen (Int based)
    fun chapterFlow(bookId: Int, chapter: Int): Flow<List<DisplayVerse>> {
        return repository.getChapter(bookId, chapter).map { verses ->
             val bookIdStr = BibleBooksMetadata.getId(bookId) ?: "GEN"
             val bookName = BibleBooksMetadata.getName(bookIdStr)
             
             verses.map { v ->
                DisplayVerse(
                    id = v.id,
                    number = v.verse,
                    text = v.text,
                    bookId = bookIdStr,
                    chapter = chapter,
                    bookName = bookName,
                    isWordsOfChrist = (v.is_red ?: 0) == 1
                )
             }
        }.flowOn(Dispatchers.IO)
    }
    
    fun updateCurrentChapterFromIndex(index: Int) {
        val (bookId, chapter) = BibleBooksMetadata.getChapterFromIndex(index)
        if (_currentBookId.value != bookId || _currentChapter.value != chapter) {
            _currentBookId.value = bookId
            _currentChapter.value = chapter
            _currentBookName.value = BibleBooksMetadata.getName(bookId)
            
            // Clear selection when changing chapters
            exitSelectionMode()

            viewModelScope.launch {
                val progress = com.biblia.koine.data.ReadingProgress(
                    currentBookId = bookId,
                    currentChapter = chapter,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateReadingProgress(progress)
            }
        }
    }
    
    fun getCurrentChapterIndex(): Int {
        return BibleBooksMetadata.getIndexFromChapter(_currentBookId.value, _currentChapter.value)
    }

    // Navigation Events (One-shot) - Legacy SharedFlow removed in favor of uiState
    // private val _scrollToVerse = ...
    
    // LRU Cache for verses (RAM) - YouVersion Optimization
    // Changed to cache RAW BibleVerse data to allow dynamic highlight merging
    private val verseCache = android.util.LruCache<String, List<com.biblia.koine.data.room.BibleVerse>>(50)
    
    // Search Job for cancellation controls
    private var searchJob: kotlinx.coroutines.Job? = null
    
    // --- EXTREME CACHING (KuroStream Optimization) ---
    private val bookMetadataCache = mutableMapOf<String, List<BibleBook>>()
    private val sessionSearchCache = LruCache<String, List<SearchResult>>(20)
    
    private val memoryCallback = object : ComponentCallbacks2 {
        override fun onTrimMemory(level: Int) {
            if (level >= 60 /* ComponentCallbacks2.TRIM_MEMORY_MODERATE */) {
                verseCache.evictAll()
                sessionSearchCache.evictAll()
                bookMetadataCache.clear()
            }
        }
        override fun onConfigurationChanged(newConfig: Configuration) {}
        override fun onLowMemory() {
            verseCache.evictAll()
            sessionSearchCache.evictAll()
            bookMetadataCache.clear()
        }
    }
    
    // --- UI STATE ---
    private val _uiState = MutableStateFlow(BibleUiState(isLoading = true))
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    init {
        // Consolidated Initialization (Master Solution v3)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _isLoading.value = true

            // 1. Wait for Database Readiness
            com.biblia.koine.data.DatabaseModule.isDatabaseReady.collect { ready ->
                if (ready) {
                    // 2. Load Versions and Settings
                    loadVersions()
                    val prefs = userPreferences.prefsFlow.first()
                    
                    // 3. Set Initial Navigation State
                    val savedBookId = prefs.lastBookId.ifBlank { "GEN" }
                    val savedChapter = if (prefs.lastChapter > 0) prefs.lastChapter else 1
                    val savedVerse = if (prefs.lastVerse > 0) prefs.lastVerse else 1
                    
                    withContext(Dispatchers.Main) {
                        if (_currentBookId.value.isEmpty()) {
                            _currentBookId.value = savedBookId
                            _currentChapter.value = savedChapter
                            _currentBookName.value = BibleBooksMetadata.getName(savedBookId)
                            
                            // Trigger content load
                            refreshData()
                            
                            // 4. Force Priority Cero: Only scroll if verse > 1
                            if (savedVerse > 1) {
                                // Keep a minimal delay ONLY for scrolling to a specific verse
                                // to ensure the list state is ready. For verse 1, we do NOTHING.
                                launch {
                                    delay(100)
                                    _uiState.update { it.copy(scrollToVerse = savedVerse to -200) }
                                    _highlightedVerse.value = savedVerse
                                }
                            }
                        }
                    }

                    // 5. Background: Warm up caches
                    launch(Dispatchers.IO) {
                        repository.optimizeDatabase()
                        loadAllBooksMetadata()
                        preloadBooksMetadata()
                        updateDailyStreak(false)
                        loadBookmarks()
                    }
                }
            }
        }

        // Observer for UiState updates during usage
        viewModelScope.launch {
            _verses.collect { displayVerses ->
                if (displayVerses.isNotEmpty()) {
                    val currentVer = userPreferences.getLastVersion() ?: "RVR1960"
                    _uiState.update { it.copy(
                        verses = displayVerses,
                        currentVersion = currentVer,
                        isLoading = false
                    ) }
                    _isLoading.value = false
                }
            }
        }
        
        // Clear highlighting logic
        viewModelScope.launch {
            _highlightedVerse.collect {
                if (it != null) {
                    delay(2000)
                    _highlightedVerse.value = null
                }
            }
        }
    }
    
    // Método para cambiar de versión y GUARDAR el ajuste (Requested)
    fun changeVersion(newVersion: String) {
        viewModelScope.launch {
            userPreferences.saveLastVersion(newVersion) // Guardamos la elección (DataStore)
            setVersion(newVersion) // Use existing logic which reloads
        }
    }

    // YOUVERSION OPTIMIZATION: Instant Navigation
    fun navigateToVerseExact(bookId: String, chapter: Int, verse: Int) {
        // 1. Update State Immediately (No reload yet)
        _currentBookId.value = bookId
        _currentChapter.value = chapter
        _currentBookName.value = BibleBooksMetadata.getName(bookId)
        
        // 2. Trigger Smart Load (Checks cache 0ms)
        refreshData()
        
        // 3. EXTREME OPTIMIZATION: Aggressive Pre-fetching (N+1, N-1)
        prefetchCurrentWindow(bookId, chapter)
        
        // 4. Scroll Event (with delay for Lazylist)
        viewModelScope.launch {
             kotlinx.coroutines.delay(100)
             _uiState.update { it.copy(scrollToVerse = verse to -200) }
             _highlightedVerse.value = verse
             
             // Persist position in background
             userPreferences.setLastPosition(bookId, chapter, verse)
        }
    }
    
    private fun prefetchCurrentWindow(bookId: String, chapter: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val version = currentVersion.value
            
            // Next chapter
            BibleBooksMetadata.getNextChapter(bookId, chapter)?.let { (nextBook, nextChap) ->
                repository.getChapterContent(nextBook, nextChap, version) // This fills repository cache
            }
            
            // Prev chapter
            BibleBooksMetadata.getPrevChapter(bookId, chapter)?.let { (prevBook, prevChap) ->
                repository.getChapterContent(prevBook, prevChap, version)
            }
        }
    }

    private suspend fun loadAllBooksMetadata() {
        val version = currentVersion.value
        if (!bookMetadataCache.containsKey(version)) {
            val booksList = repository.getBooks(version)
            bookMetadataCache[version] = booksList
            _books.value = booksList
        }
    }

    override fun onCleared() {
        super.onCleared()
        (getApplication() as Application).unregisterComponentCallbacks(memoryCallback)
    }
    
    private val _highlightedVerse = MutableStateFlow<Int?>(null)
    val highlightedVerse: StateFlow<Int?> = _highlightedVerse.asStateFlow()

    // --- MULTI-SELECT STATE (YouVersion Style) ---
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

    private val _selectedVerses = MutableStateFlow<Set<Int>>(emptySet())
    val selectedVerses: StateFlow<Set<Int>> = _selectedVerses.asStateFlow()

    val selectedVersesCount: StateFlow<Int> = _selectedVerses
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun enterSelectionMode() {
        _selectionMode.value = true
    }

    fun exitSelectionMode() {
        _selectionMode.value = false
        _selectedVerses.value = emptySet()
    }

    fun toggleVerseSelection(verseNumber: Int) {
        val current = _selectedVerses.value.toMutableSet()
        if (current.contains(verseNumber)) {
            current.remove(verseNumber)
        } else {
            current.add(verseNumber)
        }
        _selectedVerses.value = current
        
        // Auto-exit if nothing selected
        if (_selectedVerses.value.isEmpty()) {
            _selectionMode.value = false
        } else if (!_selectionMode.value) {
            _selectionMode.value = true
        }
    }

    fun selectAllVersesInChapter() {
        val allVerses = _verses.value.map { it.number }.toSet()
        _selectedVerses.value = allVerses
        _selectionMode.value = true
    }

    fun clearSelection() {
        _selectedVerses.value = emptySet()
        _selectionMode.value = false
    }

    fun copySelectedVerses(context: android.content.Context) {
        val selectedNums = _selectedVerses.value.sorted()
        if (selectedNums.isEmpty()) return

        val textToCopy = selectedNums.joinToString("\n\n") { verseNum ->
            val verse = _verses.value.find { it.number == verseNum }
            val ref = "${currentBookName.value} ${currentChapter.value}:$verseNum"
            "$ref\n${verse?.text ?: ""}"
        }

        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Versículos BibliaKoine", textToCopy)
        clipboard.setPrimaryClip(clip)
        
        exitSelectionMode()
    }

    fun shareSelectedVerses(context: android.content.Context) {
        val selectedNums = _selectedVerses.value.sorted()
        if (selectedNums.isEmpty()) return

        val shareText = selectedNums.joinToString("\n\n") { verseNum ->
            val verse = _verses.value.find { it.number == verseNum }
            val ref = "${currentBookName.value} ${currentChapter.value}:$verseNum"
            "$ref\n${verse?.text ?: ""}"
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Compartir versículos"))
        
        exitSelectionMode()
    }

    // --- USER DATA ---
    val userStats: StateFlow<UserStats> = repository.getUserStats()
        .map { it ?: UserStats() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val readingProgress: StateFlow<ReadingProgress?> = repository.getReadingProgress()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)



    
    // --- OTHERS ---
    private val _verseOfDay = MutableStateFlow<DisplayVerse?>(null)
    val verseOfDay: StateFlow<DisplayVerse?> = _verseOfDay.asStateFlow()
    
    private val _recentHighlights = MutableStateFlow<List<DisplayVerse>>(emptyList())
    val recentHighlights: StateFlow<List<DisplayVerse>> = _recentHighlights.asStateFlow()
    
    // ...
    
    fun loadRecentHighlights() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getRecentHighlights(3)
                .distinctUntilChanged()
                .collect { highlights: List<Highlight> ->
                    val displayList = mutableListOf<DisplayVerse>()
                    highlights.forEach { h ->
                        try {
                            val content = repository.getChapterContent(h.bookId, h.chapter, "RVR1960")
                            val verseData = content.verses.find { it.verse == h.verse }
                            val bookName = BibleBooksMetadata.getName(h.bookId)
                            
                            verseData?.let {
                                displayList.add(DisplayVerse(
                                    id = it.id,
                                    number = h.verse,
                                    text = it.text,
                                    bookId = h.bookId,
                                    chapter = h.chapter,
                                    bookName = bookName,
                                    highlightColor = try { 
                                        Color(android.graphics.Color.parseColor(h.color)) 
                                    } catch (e: Exception) { null }
                                ))
                            }
                        } catch (e: Exception) {
                            // Skip on error
                        }
                    }
                    _recentHighlights.value = displayList
                }
        }
    }
    

    
    // Pre-load book metadata for instant selector performance
    private suspend fun preloadBooksMetadata() = withContext(Dispatchers.IO) {
        try {
            // Warm up the cache by loading all books
            allBooks.value.forEach { book ->
                // This will cache the chapter counts
                repository.getChapters(book.id, "RVR1960")
            }
        } catch (e: Exception) {
            // Silently fail - not critical
        }
    }
    
    private suspend fun loadVersions() = withContext(Dispatchers.IO) {
        val versions = repository.getVersions()
        if (versions.isNotEmpty()) {
            _availableVersions.value = versions
        } else {
            _availableVersions.value = listOf("RVR1960", "NVI", "LBLA") // Fallback
        }
    }
    
    fun setVersion(version: String) {
        if (currentVersion.value == version) return
        
        // Clear caches to ensure fresh data
        verseCache.evictAll()
        
        viewModelScope.launch(Dispatchers.IO) {
            // Save preference - currentVersion is reactive
            userPreferences.setBibleVersion(version)
            
            // Reload books for this version
            loadBooks()
            // Force refresh of current chapter
            withContext(Dispatchers.Main) {
                refreshData()
            }
        }
    }
    
    // --- HIGHLIGHTS REFACTOR ---
    // Update local state immediately for "Instant" feel + persist to DB
    
    fun addHighlight(verse: DisplayVerse, colorHex: String) {
        addHighlights(listOf(verse), colorHex)
    }

    fun addHighlights(verses: List<DisplayVerse>, colorHex: String) {
        viewModelScope.launch {
            // 1. Persist in Base de Datos
            verses.forEach { verse ->
                repository.saveHighlight(Highlight(
                    bookId = verse.bookId,
                    chapter = verse.chapter,
                    verse = verse.number,
                    color = colorHex
                ))
            }
            
            // 2. ACTUALIZA la UI inmediatamente (Optimistic)
            val verseNumbers = verses.map { it.number }.toSet()
            val composeColor = colorHex.toComposeColor()
            _verses.update { currentVerses ->
                currentVerses.map { v ->
                    if (verseNumbers.contains(v.number)) {
                        v.copy(highlightColor = composeColor)
                    } else v
                }
            }
            
            if (selectionMode.value) {
                exitSelectionMode()
            }
        }
    }

    fun applyHighlightToSelection(colorHex: String) {
        val selected = _selectedVerses.value
        val bookId = _currentBookId.value
        val chapter = _currentChapter.value
        
        if (selected.isEmpty()) return
        
        viewModelScope.launch {
            // 1. Database Update
            selected.forEach { verseNum ->
                if (colorHex == "REMOVE") {
                    repository.removeHighlight(bookId, chapter, verseNum)
                } else {
                    repository.saveHighlight(Highlight(
                        bookId = bookId,
                        chapter = chapter,
                        verse = verseNum,
                        color = colorHex
                    ))
                }
            }
            
            // 2. State Update
            val composeColor = if (colorHex == "REMOVE") null else colorHex.toComposeColor()
            _verses.update { currentVerses ->
                currentVerses.map { v ->
                    if (selected.contains(v.number)) {
                        v.copy(highlightColor = composeColor)
                    } else v
                }
            }
            
            exitSelectionMode()
        }
    }
    
    fun updateVerseHighlight(verse: DisplayVerse, color: Color) {
        val hexColor = String.format("#%06X", (0xFFFFFF and color.value.toLong().toInt()))
        viewModelScope.launch {
            repository.updateVerseColor(verse.bookId, verse.chapter, verse.number, hexColor)
            updateLocalVerseState(verse.number) { it.copy(highlightColor = color) }
        }
    }
    
    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
            // If the deleted bookmark corresponds to current chapter, update UI
             if (bookmark.bookId == _currentBookId.value && bookmark.chapter == _currentChapter.value) {
                updateLocalVerseState(bookmark.verse) { it.copy(isBookmarked = false) }
             }
        }
    }
    
    fun removeVerseHighlight(verse: DisplayVerse) {
        removeHighlights(listOf(verse))
    }

    fun removeHighlights(verses: List<DisplayVerse>) {
        viewModelScope.launch {
            // 1. Update Database
            verses.forEach { verse ->
                repository.removeHighlight(verse.bookId, verse.chapter, verse.number)
            }
            
            // 2. Update Local State
            val verseNumbers = verses.map { it.number }.toSet()
            _verses.update { currentVerses ->
                currentVerses.map { v ->
                    if (verseNumbers.contains(v.number)) {
                        v.copy(highlightColor = null)
                    } else v
                }
            }
            
            if (selectionMode.value) {
                exitSelectionMode()
            }
        }
    }
    
    
    private fun updateLocalVerseState(verseNum: Int, transform: (DisplayVerse) -> DisplayVerse) {
        val currentList = _verses.value.toMutableList()
        val index = currentList.indexOfFirst { it.number == verseNum }
        if (index != -1) {
            currentList[index] = transform(currentList[index])
            _verses.value = currentList.toList() // Trigger StateFlow emission
        }
    }
    
    fun toggleBookmark(verse: DisplayVerse) {
        viewModelScope.launch {
            if (verse.isBookmarked) {
                // Remove
                val bookmark = _bookmarks.value.find { it.bookId == verse.bookId && it.chapter == verse.chapter && it.verse == verse.number }
                bookmark?.let { repository.removeBookmark(it) }
                updateLocalVerseState(verse.number) { it.copy(isBookmarked = false) }
            } else {
                // Add
                val bookmark = Bookmark(
                    bookId = verse.bookId,
                    chapter = verse.chapter,
                    verse = verse.number,
                    verseText = verse.text,
                    createdAt = System.currentTimeMillis()
                )
                repository.addBookmark(bookmark)
                updateLocalVerseState(verse.number) { it.copy(isBookmarked = true) }
            }
        }
    }

    fun toggleSelectedBookmarks() {
        val selected = _selectedVerses.value
        if (selected.isEmpty()) return
        
        viewModelScope.launch {
            val currentVerses = _verses.value.filter { it.number in selected }
            val bookmarks = _bookmarks.value
            
            currentVerses.forEach { verse ->
                val isBookmarked = bookmarks.any { it.bookId == verse.bookId && it.chapter == verse.chapter && it.verse == verse.number }
                if (!isBookmarked) {
                    val bookmark = Bookmark(
                        bookId = verse.bookId,
                        chapter = verse.chapter,
                        verse = verse.number,
                        verseText = verse.text,
                        createdAt = System.currentTimeMillis()
                    )
                    repository.addBookmark(bookmark)
                }
            }
            clearSelection()
            // Data will refresh via flow collection
        }
    }


    private suspend fun loadBooks() = withContext(Dispatchers.IO) {
        // Use BibleCache for instant access (YouVersion optimization)
        val loadedBooks = if (com.biblia.koine.data.cache.BibleCache.isInitialized) {
            com.biblia.koine.data.cache.BibleCache.books
        } else {
            // Fallback to repository if cache not ready yet
            repository.getBooks(currentVersion.value)
        }
        
        _books.value = loadedBooks
        
        // Initialize with SAVED book if not set (Persistence)
        if (_currentBookId.value.isEmpty() && loadedBooks.isNotEmpty()) {
            val prefs = userPreferences.prefsFlow.first()
            val savedBookId = prefs.lastBookId
            val savedChapter = prefs.lastChapter
            
            // Validate saved ID exists
            val bookToLoad = loadedBooks.find { it.id == savedBookId } ?: loadedBooks.first()
            val chapterToLoad = if (bookToLoad.id == savedBookId) savedChapter else 1
            
            _currentBookId.value = bookToLoad.id
            _currentBook.value = bookToLoad
            _currentBookName.value = bookToLoad.name
            _currentChapter.value = chapterToLoad
        } else {
            // Sync current book obj with Spanish name
            val book = loadedBooks.find { it.id == _currentBookId.value }
            if (book != null) {
                _currentBook.value = book
                _currentBookName.value = book.name
            }
        }
    }

    
    private fun loadBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllBookmarks()
                .distinctUntilChanged()
                .collect {
                    _bookmarks.value = it
                }
        }
    }

    private fun loadHighlights() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllHighlights()
                .distinctUntilChanged()
                .collect {
                    _highlights.value = it
                }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllNotes()
                .distinctUntilChanged()
                .collect {
                    _notes.value = it
                }
        }
    }

    fun navigateTo(bookId: String, chapter: Int) {
        if (bookId.isBlank()) return
        
        _currentBookId.value = bookId
        _currentChapter.value = chapter
        
        // Sync book metadata with Spanish name BEFORE refreshing data
        viewModelScope.launch {
            val book = _books.value.find { it.id == bookId }
            if (book != null) {
                _currentBook.value = book
                _currentBookName.value = book.name
            }
            refreshData()
            
            // PERSISTENCE: Save last read position
            userPreferences.setLastPosition(bookId, chapter, 1)
        }
    }
    
    // Helper/Legacy
    fun setBook(book: BibleBook) {
        navigateTo(book.id, 1)
    }

    fun navigateToVerse(bookId: String, chapter: Int, verse: Int) {
        // 1. Update State Immediately
        _currentBookId.value = bookId
        _currentChapter.value = chapter
        
        viewModelScope.launch {
            val book = _books.value.find { it.id == bookId }
            if (book != null) {
                _currentBook.value = book
                _currentBookName.value = book.name
            }
            
            // 2. Refresh Data (Smart)
            refreshData() 
            
            // 3. Emit Scroll (Fast Sync) with Offset
            _uiState.update { it.copy(scrollToVerse = verse to -200) }
            _highlightedVerse.value = verse
            
            // 4. Persist
            userPreferences.setLastPosition(bookId, chapter, verse)
        }
    }

    // Helper for Red Letters (Words of Christ)
    private fun detectChristWords(text: String): Boolean {
        // Simple heuristic as requested
        val christWords = listOf("Jesús", "Jesús dijo", "Jesús respondió", "el Señor dijo", "Hijo del Hombre")
        return christWords.any { text.contains(it, ignoreCase = true) }
    }

    private fun refreshData() {
        val bookId = _currentBookId.value
        val chapter = _currentChapter.value
        val version = currentVersion.value
        
        if (bookId.isBlank()) return
        
        clearSelection()

        viewModelScope.launch {
            try {
                 _isLoading.value = true

                // 1. GET RAW CONTENT (Cache or DB)
                val cacheKey = "$version-$bookId-$chapter"
                var rawVerses = verseCache.get(cacheKey)
                
                if (rawVerses == null) {
                    // Cache Miss - Fetch from DB
                    val content = withContext(Dispatchers.IO) {
                         repository.getChapterContent(bookId, chapter, version)
                    }
                    rawVerses = content.verses
                    _currentBookName.value = content.bookName
                    
                    // SAVE TO CACHE (Raw data only)
                    if (rawVerses.isNotEmpty()) {
                        verseCache.put(cacheKey, rawVerses)
                    }
                } else {
                    // Cache Hit (0ms) - Ensure book name is correct (cached verses don't store bookname, assuming Sync)
                     _currentBookName.value = BibleBooksMetadata.getName(bookId)
                }

                // 2. GET USER DATA (Highlights, Bookmarks) - ALWAYS FRESH
                // Using StateFlow values for speed (since they are kept in sync)
                // OR fetching fresh if needed. For "Instant", mixing memory states is best.
                
                // 2. GET USER DATA (Highlights, Bookmarks) - ALWAYS FRESH
                // Fetch specific highlights for this chapter (DB is source of truth for color persistence)
                val highlights = withContext(Dispatchers.IO) {
                    repository.getHighlightsForChapter(bookId, chapter)
                }
                
                // Fetch Pericopes (titles)
                val pericopes = withContext(Dispatchers.IO) {
                    repository.getPericopes(bookId, chapter).firstOrNull() ?: emptyList()
                }
                
                // Bookmarks & Notes (using memory cache from flows for speed)
                val bookmarks = _bookmarks.value
                val notes = _notes.value
                
                // 3. DYNAMIC MERGE (The "YouVersion" Secret)
                // Combine raw cached text + fresh user data
                val displayVerses = withContext(Dispatchers.Default) {
                    val red = showRed.value
                    val strong = showStrong.value
                    val titles = showTitles.value
                    
                    rawVerses.map { v ->
                        val highlight = highlights.find { it.verse == v.verse }
                        val highlightColor = highlight?.color?.toComposeColor()
                        
                        val isBookmarked = bookmarks.any { it.bookId == bookId && it.chapter == chapter && it.verse == v.verse }
                        val hasNote = notes.any { it.bookId == bookId && it.chapter == chapter && it.verse == v.verse }
                        
                        DisplayVerse(
                            id = v.id,
                            number = v.verse,
                            text = v.text,
                            bookId = bookId,
                            chapter = chapter,
                            bookName = _currentBookName.value,
                            highlightColor = highlightColor,
                            hasNote = hasNote,
                            isBookmarked = isBookmarked,
                            heading = if (titles) {
                                pericopes.find { it.startVerse == v.verse }?.title
                            } else null,
                            isWordsOfChrist = (v.is_red ?: 0) == 1,
                            strongs = if (strong) v.strongs else null,
                            isRed = red && (v.is_red ?: 0) == 1
                        )
                    }
                }

                _verses.value = displayVerses
                
                // Update Reading Progress
                withContext(Dispatchers.IO) {
                    repository.updateReadingProgress(com.biblia.koine.data.ReadingProgress(
                        currentBookId = bookId,
                        currentChapter = chapter,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
                
                updateDailyStreak(true)
                
                // 4. PRELOAD NEXT CHAPTER (Raw Data)
                preloadChapter(bookId, chapter + 1)
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 4. PRELOAD LOGIC
    // 4. PRELOAD LOGIC (Raw Data Cache)
    private suspend fun preloadChapter(bookId: String, nextChapter: Int) {
        val version = currentVersion.value
        val cacheKey = "$version-$bookId-$nextChapter"
        
        // If already cached, skip
        if (verseCache.get(cacheKey) != null) return
        
        withContext(Dispatchers.IO) {
             try {
                // Fetch only RAW content, no highlights/merging needed here
                val content = repository.getChapterContent(bookId, nextChapter, version)
                if (content.verses.isNotEmpty()) {
                    // Cache raw BibleVerse list
                    verseCache.put(cacheKey, content.verses)
                }
             } catch (e: Exception) {
                 // Ignore preload errors
             }
        }
    }
    
    // --- HELPERS FOR SELECTORS ---
    suspend fun getChapters(bookId: String): List<Int> {
        return repository.getChapters(bookId, currentVersion.value)
    }

    suspend fun getVersesCount(bookId: String, chapter: Int): List<Int> {
        return repository.getVersesCount(bookId, chapter, currentVersion.value)
    }

    fun updateScrollPosition(index: Int, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getReadingProgressSync()
            if (existing != null && existing.currentBookId == _currentBookId.value && existing.currentChapter == _currentChapter.value) {
                // Priority Cero: If index is 1 (Verse 1), we save 0 to keep header visible on restore
                val finalIndex = if (index <= 1) 0 else index
                val finalOffset = if (index <= 1) 0f else offset.toFloat()

                repository.updateReadingProgress(
                    existing.copy(
                        lastReadVerse = finalIndex,
                        lastScrollOffset = finalOffset,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
    
    fun nextChapter() {
        BibleBooksMetadata.getNextChapter(_currentBookId.value, _currentChapter.value)?.let { (bookId, chapter) ->
            navigateTo(bookId, chapter)
        }
    }

    fun prevChapter() {
        BibleBooksMetadata.getPrevChapter(_currentBookId.value, _currentChapter.value)?.let { (bookId, chapter) ->
            navigateTo(bookId, chapter)
        }
    }
    
    fun markChapterComplete() {
        viewModelScope.launch {
            repository.incrementChaptersRead()
            updateDailyStreak(true)
            nextChapter()
        }
    }
    
    fun onVerseHighlighted() {
        viewModelScope.launch {
            repository.incrementHighlights()
        }
    }
    
    // --- SEARCH ---
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _dictionaryResults = MutableStateFlow<List<com.biblia.koine.data.room.StrongDefinition>>(emptyList())
    val dictionaryResults: StateFlow<List<com.biblia.koine.data.room.StrongDefinition>> = _dictionaryResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    val searchHistory: StateFlow<List<com.biblia.koine.data.SearchHistory>> = repository.getSearchHistory()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ... (rest of code)

    // --- YOUVERSION-STYLE SEARCH OPTIMIZATION ---
    private val _searchTestamentFilter = MutableStateFlow<SearchTestamentFilter>(SearchTestamentFilter.ALL)
    val searchTestamentFilter: StateFlow<SearchTestamentFilter> = _searchTestamentFilter.asStateFlow()
    
    private val _totalResultsCount = MutableStateFlow(0)
    val totalResultsCount: StateFlow<Int> = _totalResultsCount.asStateFlow()
    
    
    fun setSearchTestamentFilter(filter: SearchTestamentFilter) {
        _searchTestamentFilter.value = filter
    }
    
    fun clearSearch() {
        _searchResults.value = emptyList()
        _totalResultsCount.value = 0
        _isSearching.value = false
        searchJob?.cancel()
    }

    fun search(query: String, forceRefresh: Boolean = false) {
        
        searchJob = viewModelScope.launch {
            try {
                // Debounce
                if (!forceRefresh) delay(300)
                
                _isSearching.value = true
                
                // Cache key includes filter
                val cacheKey = "${query}_${currentVersion.value}_${_searchTestamentFilter.value}"
                
                // Check cache first (YouVersion optimization)
                val cached = sessionSearchCache.get(cacheKey)
                if (!forceRefresh && cached != null) {
                    _searchResults.value = cached
                    _totalResultsCount.value = cached.size
                    _isSearching.value = false
                    return@launch
                }
                
                // Save to history
                withContext(Dispatchers.IO) {
                    repository.saveSearchToHistory(query)
                }

                // Collect from FTS5 Flow
                repository.searchBibleFTS(query, currentVersion.value).collect { ftsResults ->
                    val processedResults = withContext(Dispatchers.Default) {
                        val bookFilter = when (_searchTestamentFilter.value) {
                            SearchTestamentFilter.OLD_TESTAMENT -> (1..39)
                            SearchTestamentFilter.NEW_TESTAMENT -> (40..66)
                            SearchTestamentFilter.ALL -> null
                        }

                        val filteredResults = if (bookFilter != null) {
                            ftsResults.filter { (it.libro_id?.toIntOrNull() ?: 0) in bookFilter }
                        } else {
                            ftsResults
                        }

                        filteredResults.map { 
                            // FIX: Correct mapping from libro_id (Int as String) to Bible ID (e.g. "GEN")
                            val libroIdInt = it.libro_id?.toIntOrNull() ?: 1
                            val bookIdString = BibleBooksMetadata.getId(libroIdInt) ?: "GEN"
                            val name = BibleBooksMetadata.getName(bookIdString)
                            
                            val capituloInt = it.capitulo?.toIntOrNull() ?: 1
                            val versiculoInt = it.versiculo?.toIntOrNull() ?: 1
                            
                            SearchResult(
                                id = it.id ?: it.hashCode(),
                                reference = "$name $capituloInt:$versiculoInt",
                                text = it.contenido ?: "",
                                bookId = bookIdString,
                                chapter = capituloInt,
                                verse = versiculoInt
                            )
                        }
                    }

                    sessionSearchCache.put(cacheKey, processedResults)
                    _searchResults.value = processedResults
                    _totalResultsCount.value = processedResults.size
                }
                
            } catch (e: Exception) {
                 if (e !is kotlinx.coroutines.CancellationException) {
                     e.printStackTrace()
                     _searchResults.value = emptyList()
                 }
            } finally {
                _isSearching.value = false
            }
        }
    }
    

    
    fun deleteSearchHistory(item: com.biblia.koine.data.SearchHistory) {
        viewModelScope.launch {
            repository.deleteSearchFromHistory(item)
        }
    }
    
    // --- READING PLANS ---
    

    
    // Actions
    // Old duplicate methods removed


    fun saveNote(verse: DisplayVerse, text: String) {
         viewModelScope.launch {
            repository.saveNote(Note(
                bookId = verse.bookId,
                chapter = verse.chapter,
                verse = verse.number,
                text = text
            ))
        }
    }
    
    // Mock Verse of Day
    private fun loadVerseOfDay() {
        viewModelScope.launch {
            val goldenVerses = listOf(
                Triple("Jhn", 3, 16),
                Triple("Psa", 23, 1),
                Triple("Php", 4, 13),
                Triple("Jer", 29, 11),
                Triple("Rom", 8, 28),
                Triple("Isa", 41, 10),
                Triple("Psa", 46, 1),
                Triple("Gal", 2, 20),
                Triple("Heb", 11, 1),
                Triple("2Ti", 1, 7),
                Triple("Pro", 3, 5),
                Triple("Mat", 28, 19),
                Triple("Jos", 1, 9),
                Triple("Rom", 12, 2),
                Triple("Php", 4, 6)
            )
            
            val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            val index = dayOfYear % goldenVerses.size
            val (bookId, chapter, verseNum) = goldenVerses[index]
            
            // Fetch text
            val verses = repository.getVerses(bookId, chapter, "RVR1960").firstOrNull() // Get list
            val verseData = verses?.find { it.verse == verseNum }
            val verseText = verseData?.text ?: "Porque de tal manera amó Dios al mundo..."
            val bookName = BibleBooksMetadata.getName(bookId)
            
            val displayVerse = DisplayVerse(
                id = verseData?.id ?: (bookId.hashCode() + chapter * 1000 + verseNum),
                number = verseNum,
                text = verseText,
                bookId = bookId,
                chapter = chapter,
                bookName = bookName,
                isBookmarked = false // approximate
            )
            
            _verseOfDay.value = displayVerse
        }
    }
    
    private suspend fun updateDailyStreak(forceIncrement: Boolean = false) {
        val stats = userStats.value ?: return
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.timeInMillis
        
        val lastReadCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = stats.lastReadDate
        }
        
        // Reset time to midnight for comparison
        calendar.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        
        lastReadCalendar.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lastReadStart = lastReadCalendar.timeInMillis
        
        val diffDays = (todayStart - lastReadStart) / (24 * 60 * 60 * 1000)
        
        var newStreak = stats.daysStreak
        var shouldUpdate = false
        
        if (diffDays > 1L) {
            // Missed at least one day
            newStreak = 0
            shouldUpdate = true
        }
        
        if (forceIncrement) {
            if (diffDays == 1L || stats.lastReadDate == 0L) {
                newStreak += 1
                shouldUpdate = true
            } else if (diffDays > 1L) {
                newStreak = 1
                shouldUpdate = true
            }
        }
        
        if (shouldUpdate || (forceIncrement && diffDays != 0L)) {
             repository.updateStats(stats.copy(
                daysStreak = newStreak,
                lastReadDate = if (forceIncrement) today else stats.lastReadDate
            ))
        }
    }
    
    // --- READING PLAN MANAGEMENT ---

    

    
    // --- STRONG'S DICTIONARY ---
    private val _currentDefinition = MutableStateFlow<com.biblia.koine.data.room.StrongDefinition?>(null)
    val currentDefinition: StateFlow<com.biblia.koine.data.room.StrongDefinition?> = _currentDefinition.asStateFlow()
    private val _isLoadingDefinition = MutableStateFlow(false)
    val isLoadingDefinition: StateFlow<Boolean> = _isLoadingDefinition.asStateFlow()
    
    fun loadStrongDefinition(strongId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingDefinition.value = true
            try {
                repository.getStrongDefinition(strongId)
                    .collect { definition ->
                        _currentDefinition.value = definition
                        _isLoadingDefinition.value = false
                    }
            } catch (e: Exception) {
                _currentDefinition.value = null
                _isLoadingDefinition.value = false
            }
        }
    }
    
    fun clearDictionaryDefinition() {
        _currentDefinition.value = null
    }
    
    fun searchDictionary(query: String, callback: (List<com.biblia.koine.data.room.StrongDefinition>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = repository.searchDictionary(query)
                withContext(Dispatchers.Main) {
                    callback(results)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(emptyList())
                }
            }
        }
    }
    
    // --- PREFERENCES WRAPPERS ---
    fun changeTheme(theme: String) = viewModelScope.launch { userPreferences.setTheme(theme) }
    fun changeFontSize(size: Int) = viewModelScope.launch { userPreferences.setFontSize(size) }
    fun changeFontFamily(family: String) = viewModelScope.launch { userPreferences.setFontFamily(family) }
    fun changeLineSpacing(value: Float) = viewModelScope.launch { userPreferences.setLineSpacing(value) }
    fun changeBibleVersion(version: String) = viewModelScope.launch { userPreferences.setBibleVersion(version) }
    fun toggleRedLetters(enabled: Boolean) = viewModelScope.launch { userPreferences.setShowRedLetters(enabled) }
    fun toggleVerseNumbers(enabled: Boolean) = viewModelScope.launch { userPreferences.setShowVerseNumbers(enabled) }
    fun toggleSectionTitles(enabled: Boolean) = viewModelScope.launch { userPreferences.setShowSectionTitles(enabled) }
    fun toggleCrossRefs(enabled: Boolean) = viewModelScope.launch { userPreferences.setShowCrossReferences(enabled) }
    fun toggleJustify(enabled: Boolean) = viewModelScope.launch { userPreferences.setJustifyText(enabled) }
    fun toggleContinuous(enabled: Boolean) = viewModelScope.launch { userPreferences.setContinuousReading(enabled) }
    fun toggleKeepScreenOn(enabled: Boolean) = viewModelScope.launch { userPreferences.setKeepScreenOn(enabled) }
    fun toggleSwipeChapters(enabled: Boolean) = viewModelScope.launch { userPreferences.setSwipeChapters(enabled) }
    fun toggleAnimations(enabled: Boolean) = viewModelScope.launch { userPreferences.setAnimations(enabled) }
    fun toggleHaptic(enabled: Boolean) = viewModelScope.launch { userPreferences.setHapticFeedback(enabled) }
    fun toggleDailyVerse(enabled: Boolean) = viewModelScope.launch { userPreferences.setDailyVerse(enabled) }
    fun toggleReadingReminder(enabled: Boolean) = viewModelScope.launch { userPreferences.setReadingReminder(enabled) }
    fun changeReminderTime(time: String) = viewModelScope.launch { userPreferences.setReminderTime(time) }
    fun toggleStrongsNumbers(enabled: Boolean) = viewModelScope.launch { userPreferences.setEnableStrongs(enabled) }

    fun onScrollCompleted() {
        _uiState.update { it.copy(scrollToVerse = null) }
    }
}
