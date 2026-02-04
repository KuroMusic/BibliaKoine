package com.biblia.koine.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.data.BibleBook
import com.biblia.koine.data.Testament
import com.biblia.koine.data.bibleBooks

/**
 * Overlay for selecting Bible Book, Chapter, and Verse
 * Replaces old dialogs for a more immersive experience
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSelectorOverlay(
    isVisible: Boolean,
    currentBookId: String,
    currentChapter: Int,
    onNavigate: (String, Int, Int) -> Unit,
    onDismiss: () -> Unit,
    getVersesCount: suspend (String, Int) -> List<Int>, // Callback to get verses list
    getChapters: suspend (String) -> List<Int> // Callback to get chapters list
) {
    if (!isVisible) return

    var selectedBook by remember(currentBookId) { mutableStateOf<BibleBook?>(bibleBooks.find { it.id == currentBookId } ?: bibleBooks.firstOrNull()) }
    var selectedChapter by remember(currentChapter) { mutableStateOf<Int?>(null) }
    
    // Steps: 0 = Book, 1 = Chapter, 2 = Verse
    var step by remember { mutableStateOf(0) }
    
    // Data state
    var chapterCount by remember { mutableStateOf(0) }
    var verseCount by remember { mutableStateOf(0) }
    
    // Load chapters when book selected
    LaunchedEffect(selectedBook) {
        selectedBook?.let {
            chapterCount = getChapters(it.id).size
        }
    }
    
    // Load verses when chapter selected
    LaunchedEffect(selectedBook, selectedChapter) {
        if (selectedBook != null && selectedChapter != null) {
            verseCount = getVersesCount(selectedBook!!.id, selectedChapter!!).size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
            .clickable(enabled = false) {} // Consume clicks
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = { 
                    Text(
                        when(step) {
                            0 -> "Seleccionar Libro"
                            1 -> "${selectedBook?.name} - Capítulo"
                            2 -> "${selectedBook?.name} $selectedChapter :"
                            else -> ""
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    if (step > 0) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cerrar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            // Content
            Box(Modifier.weight(1f)) {
                when(step) {
                    0 -> BookSelectionList(
                        currentBookId = currentBookId,
                        onBookSelected = { 
                            selectedBook = it
                            step = 1
                        }
                    )
                    1 -> GridSelection(
                        count = chapterCount,
                        onSelected = {
                            selectedChapter = it
                            step = 2
                        }
                    )
                    2 -> GridSelection(
                        count = verseCount,
                        onSelected = { verse ->
                            selectedBook?.let { book ->
                                selectedChapter?.let { chap ->
                                    onNavigate(book.id, chap, verse)
                                    onDismiss()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookSelectionList(
    currentBookId: String,
    onBookSelected: (BibleBook) -> Unit
) {
    // Persistent state with rememberSaveable
    var expandedOld by rememberSaveable { mutableStateOf(false) }
    var expandedNew by rememberSaveable { mutableStateOf(false) }
    
    // Intelligent opening: Expand based on current book if both are closed
    LaunchedEffect(Unit) {
        if (!expandedOld && !expandedNew) {
            val book = bibleBooks.find { it.id == currentBookId }
            if (book != null) {
                if (book.testament == Testament.OLD) expandedOld = true else expandedNew = true
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ANTIGUO TESTAMENTO
        item {
            ExpansionHeader(
                title = "Antiguo Testamento",
                subtitle = "39 libros",
                icon = Icons.Default.MenuBook,
                isExpanded = expandedOld,
                onToggle = { 
                    expandedOld = !expandedOld
                    if (expandedOld) expandedNew = false // Accordion behavior
                }
            )
        }
        
        if (expandedOld) {
            items(
                items = bibleBooks.filter { it.testament == Testament.OLD },
                key = { it.id }
            ) { book ->
                BookItem(book, onBookSelected)
            }
        }
        
        // NUEVO TESTAMENTO
        item {
            ExpansionHeader(
                title = "Nuevo Testamento",
                subtitle = "27 libros",
                icon = Icons.Default.Bookmark,
                isExpanded = expandedNew,
                onToggle = { 
                    expandedNew = !expandedNew
                    if (expandedNew) expandedOld = false // Accordion behavior
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (expandedNew) {
            items(
                items = bibleBooks.filter { it.testament == Testament.NEW },
                key = { it.id }
            ) { book ->
                BookItem(book, onBookSelected)
            }
        }
    }
}

@Composable
private fun ExpansionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFFD4AF37), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookItem(book: BibleBook, onClick: (BibleBook) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick(book) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFD4AF37).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${book.chapters} cap",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFD4AF37), // Premium Gold
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GridSelection(
    count: Int,
    onSelected: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(60.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count) { index ->
            val num = index + 1
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .clickable { onSelected(num) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$num",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
