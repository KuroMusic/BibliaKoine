package com.biblia.koine.ui.screens

import android.content.Context
import android.content.Intent
import com.biblia.koine.ui.components.VerseImageDialog
import com.biblia.koine.ui.components.WordStudyBottomSheet
import com.biblia.koine.utils.VerseImageGenerator
import com.biblia.koine.data.prefs.UserPreferences
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.viewmodel.DisplayVerse
import com.biblia.koine.viewmodel.SearchTestamentFilter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.animation.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.utils.getBookNameSpanish
import com.biblia.koine.data.BibleBooksMetadata
import com.biblia.koine.ui.components.*
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.selection.SelectionContainer

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun BibleScreen(
    viewModel: BibleViewModel, 
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLexicon: (String) -> Unit
) {
    // ViewModel State Collection (Moved to Top)
    val verses by viewModel.verses.collectAsState()
    val book by viewModel.currentBook.collectAsState()
    val chapter by viewModel.currentChapter.collectAsState()
    val version by viewModel.currentVersion.collectAsState()
    val availableVersions by viewModel.availableVersions.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    val prefs by viewModel.userPrefs.collectAsState()
    
    val highlightedVerse by viewModel.highlightedVerse.collectAsState()
    val headerTitle by viewModel.currentBookName.collectAsState()
    val currentBookId by viewModel.currentBookId.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()

    // UI State
    var showMenu by remember { mutableStateOf(false) }
    var showVersionMenu by remember { mutableStateOf(false) }
    var showBookSelector by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }

    // Immersive Scroll State
    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.toPx() }
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }
    
    // TopBar Offset
    val topBarHeight = 64.dp
    val topBarHeightPx = with(LocalDensity.current) { topBarHeight.toPx() }
    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }

    // State for actions
    var activeVerseForHighlight by remember { mutableStateOf<DisplayVerse?>(null) }
    var activeVerseForNote by remember { mutableStateOf<DisplayVerse?>(null) }
    
    // Premium Features State
    var showVerseImageDialog by remember { mutableStateOf(false) }
    var currentVerseForImage by remember { mutableStateOf<DisplayVerse?>(null) }
    var showDictionaryOverlay by remember { mutableStateOf(false) }
    
    // Highlight Picker State
    var showHighlightPicker by remember { mutableStateOf(false) }

    // HorizontalPager state
    val initialPage = remember { BibleBooksMetadata.getIndexFromChapter(currentBookId, chapter) }
    val pagerState = rememberPagerState(initialPage = initialPage) { BibleBooksMetadata.getTotalChapters() }
    
    // Multi-selection state
    val selectedVerses by viewModel.selectedVerses.collectAsState()
    val isSelectionMode = selectedVerses.isNotEmpty() // Derived from VM state
    
    // Dictionary state
    val currentDefinition by viewModel.currentDefinition.collectAsState()
    
    // Word Selector State (Moved to Top)
    var showWordSelector by remember { mutableStateOf(false) }
    var verseForWordSelector by remember { mutableStateOf<DisplayVerse?>(null) }
    var showStudyDialog by remember { mutableStateOf(false) }
    var selectedWordForStudy by remember { mutableStateOf("") }

    // Derived State for UI Blocking
    val isOverlayVisible = showBookSelector || showQuickSettings || activeVerseForHighlight != null || activeVerseForNote != null || isSelectionMode || showVerseImageDialog || showDictionaryOverlay

    // CRITICAL FIX: Ensure bars are visible when overlays are active
    LaunchedEffect(isOverlayVisible) {
        if (isOverlayVisible) {
            bottomBarOffsetHeightPx = 0f
            topBarOffsetHeightPx = 0f
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newBottomOffset = bottomBarOffsetHeightPx - delta
                bottomBarOffsetHeightPx = newBottomOffset.coerceIn(0f, bottomBarHeightPx)
                
                val newTopOffset = topBarOffsetHeightPx + delta
                topBarOffsetHeightPx = newTopOffset.coerceIn(-topBarHeightPx, 0f)
                
                return Offset.Zero
            }
        }
    }
    

    
    // Alpha Calculations (Optimized)
    val topBarAlpha by remember {
        derivedStateOf { 1f - (-topBarOffsetHeightPx / topBarHeightPx).coerceIn(0f, 1f) }
    }
    val bottomBarAlpha by remember {
        derivedStateOf { 1f - (bottomBarOffsetHeightPx / bottomBarHeightPx).coerceIn(0f, 1f) }
    }

    // State for actions logic (now derived)
    // Variables moved up
    
    val context = LocalContext.current
    
    // BACK HANDLER
    // Prioritize closing overlays before navigation
    BackHandler(enabled = showBookSelector || showQuickSettings || isSelectionMode || showVerseImageDialog || showDictionaryOverlay || activeVerseForNote != null || showStudyDialog) {
        when {
            showStudyDialog -> showStudyDialog = false
            showBookSelector -> showBookSelector = false
            showQuickSettings -> showQuickSettings = false
            isSelectionMode -> viewModel.clearSelection()
            showVerseImageDialog -> showVerseImageDialog = false
            showDictionaryOverlay -> showDictionaryOverlay = false
            activeVerseForNote != null -> activeVerseForNote = null
        }
    }

    // --- PAGER SYNC ---
    // 1. Sync Pager -> ViewModel (When user swipes)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { pageIndex ->
                // Sync to ViewModel only when the user is driving the pager
                // This prevents the loop when ViewModel updates pager programmaticly
                if (pagerState.interactionSource.interactions.firstOrNull { it is androidx.compose.foundation.interaction.DragInteraction.Start } != null || pagerState.isScrollInProgress) {
                     // We double check if it's already the current state in the VM to be safe
                     viewModel.updateCurrentChapterFromIndex(pageIndex)
                }
            }
    }

    // 2. Sync ViewModel -> Pager (When user selects book/chapter)
    LaunchedEffect(currentBookId, chapter) {
        val targetPage = BibleBooksMetadata.getIndexFromChapter(currentBookId, chapter)
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            // Use immediate scroll for direct navigation to avoid loop through pages
            pagerState.scrollToPage(targetPage)
        }
    }

    // Reset Selection on Book/Chapter Change
    LaunchedEffect(currentBookId, chapter) {
         viewModel.clearSelection()
    }
    
    // CRITICAL FIX: Clean up selection mode when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
             viewModel.clearSelection()
        }
    }

    LaunchedEffect(highlightedVerse) {
        highlightedVerse?.let { 
            // Hide Bars (Immersive Mode) when a verse is highlighted/scrolled to
            bottomBarOffsetHeightPx = bottomBarHeightPx
            topBarOffsetHeightPx = -topBarHeightPx
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val uiState by viewModel.uiState.collectAsState()
            
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !isOverlayVisible,
                    key = { it }
                ) { page ->
                    val pageVerses by remember(page) { viewModel.getChapterFlow(page) }.collectAsState(initial = emptyList())
                    val (pageBookId, pageChapter) = remember(page) { BibleBooksMetadata.getChapterFromIndex(page) }
                    val pageBookName = remember(pageBookId) { BibleBooksMetadata.getName(pageBookId) }
                    
                    val pageListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

                // --- SINCRONIZACIÓN DE NAVEGACIÓN (Buscador/Acciones) ---
                // CRITICAL: Solo reaccionar cuando scrollToVerse cambia, NO cuando pageVerses cambia
                LaunchedEffect(uiState.scrollToVerse) {
                    val scrollEvent = uiState.scrollToVerse
                    if (scrollEvent != null) {
                        val (verseNum, offset) = scrollEvent
                        
                        // Esperar a que los datos estén disponibles
                        if (pageVerses.isNotEmpty()) {
                            val verseIndex = pageVerses.indexOfFirst { it.number == verseNum }
                            
                            if (verseIndex >= 0) {
                                // Si es el versículo 1, mostrar el header (item 0)
                                if (verseNum == 1) {
                                    pageListState.animateScrollToItem(0, 0)
                                } else {
                                    // index + 1 porque ChapterHeader está en item 0
                                    pageListState.animateScrollToItem(verseIndex + 1, offset)
                                }
                                
                                // CRITICAL: Avisar al ViewModel que el evento se consumió
                                viewModel.onScrollCompleted()
                            }
                        }
                    }
                }
                
                BibleChapterPage(
                    viewModel = viewModel,
                        verses = pageVerses,
                        headerTitle = pageBookName,
                        chapter = pageChapter,
                        listState = pageListState,
                    topPadding = topBarHeight,
                    bottomPadding = 120.dp,
                    prefs = prefs, // Pass prefs object
                    readingProgress = readingProgress,
                    onScroll = { index, offset -> viewModel.updateScrollPosition(index, offset) },
                    isSelectionMode = isSelectionMode,
                    selectedVerses = selectedVerses,
                    highlightedVerse = highlightedVerse,
                    onSelectionToggle = { v ->
                        viewModel.toggleVerseSelection(v.number)
                    },
                    onAction = { action ->
                        when(action) {
                            is VerseAction.Highlight -> {
                                activeVerseForHighlight = action.verse
                                showHighlightPicker = true // Open picker instead of direct add
                            }
                            is VerseAction.AddNote -> activeVerseForNote = action.verse
                            is VerseAction.Bookmark -> viewModel.toggleBookmark(action.verse)
                            is VerseAction.Share -> shareToWhatsApp(context, action.verse)
                            is VerseAction.Copy -> copyVerseToClipboard(context, action.verse)
                            is VerseAction.CreateImage -> { 
                                currentVerseForImage = action.verse
                                showVerseImageDialog = true
                            }
                            is VerseAction.LookupStrong -> {
                                viewModel.loadStrongDefinition(action.strongNumber)
                                showDictionaryOverlay = true
                            }
                            is VerseAction.Study -> {
                                verseForWordSelector = action.verse
                                showWordSelector = true
                            }
                        }
                    },
                    enableInteraction = !isOverlayVisible
                )
            }
        }
    }
        
        // Legacy SelectionActionBar removed - now handled by BibleScreenTopBar
        
        // --- TOP BAR (Floating/Auto-Hide) ---
         Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight)
                .offset { IntOffset(0, topBarOffsetHeightPx.roundToInt()) }
                .graphicsLayer { alpha = topBarAlpha }
         ) {
             BibleScreenTopBar(
                 viewModel = viewModel,
                 onNavigateToSearch = onNavigateToSearch,
                 onNavigateToSettings = onNavigateToSettings,
                 onShowQuickSettings = { showQuickSettings = true },
                 onShowHighlightMenu = {
                     if (selectedVerses.isNotEmpty()) {
                        activeVerseForHighlight = verses.find { it.number == selectedVerses.first() }
                        showHighlightPicker = true
                     }
                 }
             )
         }


        // --- BOTTOM BAR (Floating/Auto-Hide) ---
        if (!isSelectionMode) {
            ChapterNavigationCapsule(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .offset { IntOffset(0, bottomBarOffsetHeightPx.roundToInt()) }
                    .graphicsLayer { alpha = bottomBarAlpha },
                book = headerTitle,
                chapter = chapter,
                onPrev = { viewModel.prevChapter() },
                onNext = { viewModel.nextChapter() },
                onOpenSelector = { showBookSelector = true }
            )
        }

        // --- OVERLAYS ---
        
        var selectedWordForStudy by remember { mutableStateOf("") }

        // Book Selector
        BookSelectorOverlay(
            isVisible = showBookSelector,
            currentBookId = currentBookId,
            currentChapter = chapter,
            onNavigate = { b, c, v -> viewModel.navigateToVerse(b, c, v) },
            onDismiss = { showBookSelector = false },
            getChapters = { viewModel.getChapters(it) },
            getVersesCount = { b, c -> viewModel.getVersesCount(b, c) }
        )

        // Quick Settings
        AnimatedVisibility(
            visible = showQuickSettings,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            QuickSettingsOverlay(
                prefs = prefs,
                onFontSizeChange = { viewModel.changeFontSize(it.toInt()) },
                onToggleRedLetters = { viewModel.toggleRedLetters(it) },
                onToggleStrongs = { viewModel.toggleStrongsNumbers(it) },
                onToggleTitles = { viewModel.toggleSectionTitles(it) },
                onToggleVerseNumbers = { viewModel.toggleVerseNumbers(it) },
                onDismiss = { showQuickSettings = false }
            )
        }

        // Action Dialogs
        activeVerseForNote?.let { verse ->
            val existingNote = viewModel.notes.value.find { 
                it.bookId == verse.bookId && it.chapter == verse.chapter && it.verse == verse.number 
            }?.text ?: ""
            NoteEditorDialog(
                verse = verse,
                existingNote = existingNote,
                onSave = { text -> viewModel.saveNote(verse, text) },
                onDismiss = { activeVerseForNote = null }
            )
        }

        currentVerseForImage?.let { verse ->
            VerseImageDialog(
                verse = verse,
                isVisible = showVerseImageDialog,
                onDismiss = {
                    showVerseImageDialog = false
                    currentVerseForImage = null
                }
            )
        }


        if (showDictionaryOverlay) {
            WordStudyBottomSheet(
                word = selectedWordForStudy,
                strongDefinition = currentDefinition,
                onDismiss = {
                    showDictionaryOverlay = false
                    viewModel.clearDictionaryDefinition()
                },
                onViewConcordance = { onNavigateToSearch() }
            )
        }
        
        if (showWordSelector && verseForWordSelector != null) {
            VerseWordSelectorBottomSheet(
                verse = verseForWordSelector!!,
                onDismiss = { 
                    showWordSelector = false
                    verseForWordSelector = null
                },
                onWordSelected = { word ->
                    selectedWordForStudy = word
                    showStudyDialog = true
                }
            )
        }

        if (showStudyDialog) {
            val goldColor = Color(0xFFD4AF37)
            AlertDialog(
                onDismissRequest = { showStudyDialog = false },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showStudyDialog = false }) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                },
                title = {
                    Text(
                        "¿Cómo quieres estudiar esta palabra?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "Seleccionado: \"$selectedWordForStudy\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Surface(
                            onClick = {
                                showStudyDialog = false
                                viewModel.searchDictionary(selectedWordForStudy) { results ->
                                    if (results.isNotEmpty()) {
                                        onNavigateToLexicon(selectedWordForStudy)
                                    } else {
                                        android.widget.Toast.makeText(context, "No se encontró '$selectedWordForStudy' en el léxico", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.MenuBook, null, tint = goldColor, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Ver en Léxico BibliaKoine", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Surface(
                            onClick = {
                                showStudyDialog = false
                                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                    putExtra(android.app.SearchManager.QUERY, selectedWordForStudy)
                                }
                                context.startActivity(intent)
                            },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Search, null, tint = goldColor, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Buscar en Google", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = goldColor,
                textContentColor = Color.White
            )
        }

        // Highlight Picker
        if (showHighlightPicker && activeVerseForHighlight != null) {
            HighlightPickerBottomSheet(
                onDismiss = {
                    showHighlightPicker = false
                    activeVerseForHighlight = null
                },
                onColorSelected = { hexColor ->
                    if (isSelectionMode) {
                        viewModel.applyHighlightToSelection(hexColor)
                    } else {
                        activeVerseForHighlight?.let { verse ->
                            if (hexColor == "REMOVE") {
                                viewModel.removeVerseHighlight(verse)
                            } else {
                                viewModel.addHighlight(verse, hexColor)
                            }
                        }
                    }
                    showHighlightPicker = false
                    activeVerseForHighlight = null
                }
            )
        }
    }
}
@Composable
fun QuickSettingsOverlay(
    prefs: com.biblia.koine.data.prefs.UserPreferences.Prefs,
    onFontSizeChange: (Float) -> Unit,
    onToggleRedLetters: (Boolean) -> Unit,
    onToggleStrongs: (Boolean) -> Unit,
    onToggleTitles: (Boolean) -> Unit,
    onToggleVerseNumbers: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { }
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ajustes de Lectura",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Tamaño de Fuente: ${prefs.fontSize}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = prefs.fontSize.toFloat(),
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..36f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFD4AF37),
                        activeTrackColor = Color(0xFFD4AF37)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsToggle("Rojo Jesús", prefs.showRedLetters, onToggleRedLetters)
                SettingsToggle("Números Strong", prefs.enableStrongs, onToggleStrongs)
                SettingsToggle("Títulos de sección", prefs.showSectionTitles, onToggleTitles)
                SettingsToggle("Números de versículo", prefs.showVerseNumbers, onToggleVerseNumbers)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFD4AF37),
                checkedTrackColor = Color(0xFFD4AF37).copy(alpha = 0.5f)
            )
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreenTopBar(
    viewModel: BibleViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onShowQuickSettings: () -> Unit,
    onShowHighlightMenu: () -> Unit
) {
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedCount by viewModel.selectedVersesCount.collectAsState()
    val currentVersion by viewModel.currentVersion.collectAsState()
    val availableVersions by viewModel.availableVersions.collectAsState()
    
    val bookName by viewModel.currentBookName.collectAsState()
    val chapter by viewModel.currentChapter.collectAsState()
    
    var showVersionMenu by remember { mutableStateOf(false) }
    var showExtraMenu by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    TopAppBar(
        windowInsets = WindowInsets(0),
        title = {
            if (selectionMode) {
                Text(
                    text = "${selectedCount} seleccionado${if (selectedCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateContentSize()
                ) {
                    Text(
                        text = "$bookName $chapter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Version Pill
                    Box {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.clickable { showVersionMenu = true }
                        ) {
                            Text(
                                text = currentVersion,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showVersionMenu,
                            onDismissRequest = { showVersionMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            availableVersions.forEach { ver ->
                                DropdownMenuItem(
                                    text = { Text(ver, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.setVersion(ver)
                                        showVersionMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = { viewModel.exitSelectionMode() }) {
                    Icon(Icons.Rounded.Close, "Cerrar", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        actions = {
            if (selectionMode) {
                IconButton(onClick = { viewModel.copySelectedVerses(context) }) {
                    Icon(Icons.Rounded.ContentCopy, "Copiar", tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = { viewModel.shareSelectedVerses(context) }) {
                    Icon(Icons.Rounded.Share, "Compartir", tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = onShowHighlightMenu) {
                    Icon(Icons.Rounded.Highlight, "Destacar", tint = MaterialTheme.colorScheme.onBackground)
                }
                SelectAllButton(viewModel)
            } else {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Rounded.Search, "Buscar", tint = MaterialTheme.colorScheme.onBackground)
                }
                Box {
                    IconButton(onClick = { showExtraMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, "Menú", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    DropdownMenu(
                        expanded = showExtraMenu,
                        onDismissRequest = { showExtraMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configuración", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                showExtraMenu = false
                                onNavigateToSettings()
                            },
                            leadingIcon = { Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.onSurface) }
                        )
                        DropdownMenuItem(
                            text = { Text("Apariencia", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                showExtraMenu = false
                                onShowQuickSettings()
                            },
                            leadingIcon = { Icon(Icons.Rounded.FormatSize, null, tint = MaterialTheme.colorScheme.onSurface) }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun SelectAllButton(viewModel: BibleViewModel) {
    val selectedCount by viewModel.selectedVersesCount.collectAsState()
    val verses by viewModel.verses.collectAsState()
    val totalVerses = verses.size
    
    val isAllSelected = selectedCount == totalVerses && totalVerses > 0
    
    TextButton(onClick = {
        if (isAllSelected) {
            viewModel.clearSelection()
        } else {
            viewModel.selectAllVersesInChapter()
        }
    }) {
        Text(
            text = if (isAllSelected) "Deseleccionar" else "Toda",
            color = Color(0xFFD4AF37),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChapterHeader(book: String, chapter: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .focusable(), // Master Solution v2: Capture focus here to prevent list jumps
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = book,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light,
                fontSize = 26.sp
            )
        )
        Text(
            text = chapter.toString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 68.sp,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun ChapterNavigationCapsule(
    modifier: Modifier,
    book: String,
    chapter: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenSelector: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Rounded.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurface)
            }
             
            // Clickable Central Title
            Text(
                text = "$book $chapter",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clickable { onOpenSelector() },
                fontWeight = FontWeight.Medium
            )
            
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BibleChapterPage(
    viewModel: BibleViewModel,
    verses: List<DisplayVerse>,
    headerTitle: String,
    chapter: Int,
    listState: LazyListState,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    prefs: com.biblia.koine.data.prefs.UserPreferences.Prefs,
    readingProgress: com.biblia.koine.data.ReadingProgress?,
    onScroll: (Int, Int) -> Unit,
    isSelectionMode: Boolean,
    selectedVerses: Set<Int>,
    highlightedVerse: Int?,
    onSelectionToggle: (DisplayVerse) -> Unit,
    onAction: (VerseAction) -> Unit,
    enableInteraction: Boolean = true
) {
    // 1. Scroll Persistence (Debounced)
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset 
        }
        .debounce(1000) // Save after 1s of no scrolling
        .collectLatest { (index, offset) ->
            if (index > 0 || offset > 0) {
                onScroll(index, offset)
            }
        }
    }
    
    // 2. Scroll Restoration (One-time)
    var hasRestored by rememberSaveable(chapter) { mutableStateOf(false) }
    
    LaunchedEffect(readingProgress, verses) {
        if (!hasRestored && readingProgress != null && verses.isNotEmpty()) {
            val firstVerse = verses.first()
            if (readingProgress.currentBookId == firstVerse.bookId && readingProgress.currentChapter == chapter) {
                if (readingProgress.lastReadVerse > 0) {
                    // +1 because index 0 is the Header
                    // But if saved index INCLUDES header...
                    // Let's assume saved index is raw list index.
                    listState.scrollToItem(readingProgress.lastReadVerse, readingProgress.lastScrollOffset.toInt())
                    hasRestored = true
                }
            }
        }
    }
    
    if (enableInteraction) {
        BibleVerseListContent(
            viewModel = viewModel,
            verses = verses,
            headerTitle = headerTitle,
            chapter = chapter,
            listState = listState,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            prefs = prefs,
            isSelectionMode = isSelectionMode,
            selectedVerses = selectedVerses,
            highlightedVerse = highlightedVerse,
            onSelectionToggle = onSelectionToggle,
            onAction = onAction
        )
    } else {
        BibleVerseListContent(
            viewModel = viewModel,
            verses = verses,
            headerTitle = headerTitle,
            chapter = chapter,
            listState = listState,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            prefs = prefs,
            isSelectionMode = false,
            selectedVerses = emptySet(),
            highlightedVerse = null,
            onSelectionToggle = {},
            onAction = {}
        )
    }
}

@Composable
fun BibleVerseListContent(
    viewModel: BibleViewModel,
    verses: List<DisplayVerse>,
    headerTitle: String,
    chapter: Int,
    listState: LazyListState,
    topPadding: androidx.compose.ui.unit.Dp,
    bottomPadding: androidx.compose.ui.unit.Dp,
    prefs: com.biblia.koine.data.prefs.UserPreferences.Prefs,
    isSelectionMode: Boolean,
    selectedVerses: Set<Int>,
    highlightedVerse: Int?,
    onSelectionToggle: (DisplayVerse) -> Unit,
    onAction: (VerseAction) -> Unit
) {
    val context = LocalContext.current
    
    // Auto-scroll to highlighted verse
    // Auto-scroll logic removed from here as it's now handled by viewModel.scrollToVerse in BibleScreen

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        // Sincronización de Scroll: Reducir padding para que el header sea visible
        contentPadding = PaddingValues(
            top = topPadding, // Solo el TopAppBar, sin padding extra
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header (Book name)
        item(key = "chapter_header") {
            ChapterHeader(headerTitle, chapter)
            Spacer(Modifier.height(24.dp))
        }

    // ... inside BibleChapterPage ...
        items(
            items = verses,
            key = { it.id },
            contentType = { "verse" }
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
                shouldHighlight = highlightedVerse == verse.number,
                onAction = onAction
            )
        }
    }
}

// Helper Functions
fun activeVerseText(verse: DisplayVerse): String {
   return verse.text.take(50) + "..."
}

fun shareToWhatsApp(context: Context, verse: DisplayVerse) {
    val text = "\"${verse.text}\" - ${verse.reference} (RV1960)\n\nDescargado desde Biblia Koine"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Compartir versículo"))
    }
}

fun cleanText(text: String): String {
    return text.replace(Regex("<[^>]*>"), "").replace(Regex("\\s+"), " ").trim()
}

fun copyVerseToClipboard(context: Context, verse: DisplayVerse) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val text = cleanText(verse.text)
    val clip = android.content.ClipData.newPlainText("Versículo", "${verse.reference}\n$text")
    clipboard.setPrimaryClip(clip)
}

fun copyMultipleVerses(context: Context, verses: List<DisplayVerse>, book: String, chapter: Int) {
    if (verses.isEmpty()) return
    
    val sortedVerses = verses.sortedBy { it.number }
    val verseRange = if (sortedVerses.size == 1) {
        "${sortedVerses.first().number}"
    } else {
        "${sortedVerses.first().number}-${sortedVerses.last().number}"
    }
    
    val text = buildString {
        appendLine("$book $chapter:$verseRange")
        appendLine()
        sortedVerses.forEach { verse ->
            val clean = cleanText(verse.text)
            appendLine("${verse.number}. $clean")
        }
    }
    
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Versículos", text)
    clipboard.setPrimaryClip(clip)
    
    android.widget.Toast.makeText(context, "${verses.size} versículos copiados", android.widget.Toast.LENGTH_SHORT).show()
}

fun shareMultipleVerses(context: Context, verses: List<DisplayVerse>, book: String, chapter: Int) {
    if (verses.isEmpty()) return
    
    val sortedVerses = verses.sortedBy { it.number }
    val verseRange = if (sortedVerses.size == 1) {
        "${sortedVerses.first().number}"
    } else {
        "${sortedVerses.first().number}-${sortedVerses.last().number}"
    }
    
    val text = buildString {
        appendLine("$book $chapter:$verseRange")
        appendLine()
        sortedVerses.forEach { verse ->
            appendLine("${verse.number}. ${verse.text}")
        }
        appendLine()
        appendLine("Descargado desde Biblia Koine")
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir versículos"))
}

@Composable
fun CrossReferencesBottomSheetContent(
    verse: DisplayVerse,
    onReferenceClick: (com.biblia.koine.data.ScriptureReference) -> Unit
) {
    val refs = com.biblia.koine.data.CrossReferencesData.getReferences(
        verse.bookId, 
        verse.chapter, 
        verse.number
    )
    
    Column(Modifier.padding(16.dp)) {
        Text(
            "Referencias Cruzadas",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFD4AF37)
        )
        Text(
            "${verse.bookName} ${verse.chapter}:${verse.number}: ${activeVerseText(verse)}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            maxLines = 2
        )
        Spacer(Modifier.height(16.dp))
        
        if (refs.isEmpty()) {
            Text("No se encontraron referencias directas.", color = Color.Gray, modifier = Modifier.padding(bottom=32.dp))
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                items(
                    items = refs,
                    key = { "${it.bookId}_${it.chapter}_${it.verse}" }
                ) { ref ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReferenceClick(ref) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.List, 
                            null, 
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "${ref.bookId} ${ref.chapter}:${ref.verse}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Ver versículo",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

