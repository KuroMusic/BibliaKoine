package com.biblia.koine.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.ui.components.VerseItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    bookId: Int, 
    chapter: Int, 
    targetVerse: Int,
    viewModel: BibleViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val verses by viewModel.chapterFlow(bookId, chapter).collectAsState(emptyList())
    val prefs by viewModel.userPrefs.collectAsState()
    
    // GUARD: Variable de estado para controlar que el scroll suceda solo UNA vez
    var hasScrolled by remember(bookId, chapter, targetVerse) { mutableStateOf(false) }
    
    // Scroll Behavior para esconder la barra al scrollear (Fix "barra estorba")
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    
    // Highlight Animation State - Reset on navigation
    var activeVerseId by remember(bookId, chapter, targetVerse) { mutableStateOf<Int?>(null) }

    LaunchedEffect(targetVerse, verses.isNotEmpty()) {
        if (!hasScrolled && targetVerse > 0 && verses.isNotEmpty()) {
            val index = verses.indexOfFirst { it.number == targetVerse }
            if (index >= 0) {
                listState.animateScrollToItem(index)
                hasScrolled = true
                
                // Trigger Golden Flash
                activeVerseId = targetVerse
                kotlinx.coroutines.delay(2000)
                activeVerseId = null
            }
        }
    }

    // State sync with ViewModel to ensure global consistency
    LaunchedEffect(bookId, chapter) {
        val bookIdStr = com.biblia.koine.data.BibleBooksMetadata.getId(bookId) ?: "GEN"
        viewModel.navigateTo(bookIdStr, chapter)
    }
    
    val bookName = remember(bookId) { 
        val id = com.biblia.koine.data.BibleBooksMetadata.getId(bookId) ?: "GEN"
        com.biblia.koine.data.BibleBooksMetadata.getName(id) 
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background, // Match App Theme
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "$bookName $chapter",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp) // Removed generic spacing, handled by items
        ) {
             items(
                items = verses,
                key = { it.id }
            ) { verse ->
                VerseItem(
                    verse = verse,
                    viewModel = viewModel,
                    prefs = prefs,
                    showNumbers = prefs.showVerseNumbers,
                    showSectionTitles = prefs.showSectionTitles,
                    showRedLetters = prefs.showRedLetters,
                    showStrongs = prefs.enableStrongs,
                    justify = prefs.justifyText,
                    shouldHighlight = (activeVerseId == verse.number), 
                    onAction = { /* Read-only view - navigate to main Bible screen for interactions */ }
                )
            }
        }
    }
}
