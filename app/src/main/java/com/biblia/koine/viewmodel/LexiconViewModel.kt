package com.biblia.koine.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biblia.koine.data.repository.BibleRepository
import com.biblia.koine.data.room.StrongDefinition
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LexiconViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BibleRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<StrongDefinition>>(emptyList())
    val searchResults: StateFlow<List<StrongDefinition>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Reactive search with debounce
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .onEach { _isLoading.value = true }
                .mapLatest { query ->
                    if (query.length < 2) emptyList()
                    else repository.searchDictionary(query)
                }
                .collect { results ->
                    _searchResults.value = results
                    _isLoading.value = false
                }
        }
    }

    private val _selectedWord = MutableStateFlow<StrongDefinition?>(null)
    val selectedWord: StateFlow<StrongDefinition?> = _selectedWord.asStateFlow()

    private val _versePreview = MutableStateFlow<String?>(null)
    val versePreview: StateFlow<String?> = _versePreview.asStateFlow()

    private val _isShowingPreview = MutableStateFlow(false)
    val isShowingPreview: StateFlow<Boolean> = _isShowingPreview.asStateFlow()

    private val _previewReference = MutableStateFlow("")
    val previewReference: StateFlow<String> = _previewReference.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query.trim()
    }

    fun loadWord(topic: String) {
        viewModelScope.launch {
            repository.getStrongDefinition(topic).collect {
                _selectedWord.value = it
            }
        }
    }

    fun fetchVersePreview(bookId: String, chapter: Int, verse: Int, reference: String) {
        viewModelScope.launch {
            val content = repository.getChapterContent(bookId, chapter, "RVR1960") // Default to RVR for preview
            val verseText = content.verses.find { it.verse == verse }?.text ?: "Versículo no encontrado"
            _versePreview.value = verseText
            _previewReference.value = reference
            _isShowingPreview.value = true
        }
    }

    fun dismissPreview() {
        _isShowingPreview.value = false
        _versePreview.value = null
    }
}
