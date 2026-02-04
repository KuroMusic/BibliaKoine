package com.biblia.koine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.viewmodel.SearchResult
import com.biblia.koine.viewmodel.SearchTestamentFilter
import com.biblia.koine.ui.components.SearchResultItem
import kotlinx.coroutines.delay

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: BibleViewModel, 
    onNavigateToVerse: (SearchResult) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val results: List<SearchResult> by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val currentFilter by viewModel.searchTestamentFilter.collectAsState()
    val totalCount by viewModel.totalResultsCount.collectAsState()
    val currentVersion by viewModel.currentVersion.collectAsState()
    
    // DEBOUNCE 300ms (YouVersion Style)
    // RE-SEARCH when query, filter OR version changes
    LaunchedEffect(query, currentFilter, currentVersion) {
        if (query.length >= 2) {
            viewModel.search(query)
        }
    }

    // Clear search results when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearch()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar en la Biblia") },
            placeholder = { Text("Ej: Amor, Fe, Esperanza...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = ""; viewModel.search("") }) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        // Filters Row (RESORED)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == SearchTestamentFilter.ALL,
                onClick = { viewModel.setSearchTestamentFilter(SearchTestamentFilter.ALL) },
                label = { Text("Toda") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = currentFilter == SearchTestamentFilter.OLD_TESTAMENT,
                onClick = { viewModel.setSearchTestamentFilter(SearchTestamentFilter.OLD_TESTAMENT) },
                label = { Text("Antiguo T.") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = currentFilter == SearchTestamentFilter.NEW_TESTAMENT,
                onClick = { viewModel.setSearchTestamentFilter(SearchTestamentFilter.NEW_TESTAMENT) },
                label = { Text("Nuevo T.") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        
        if (isSearching) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (query.length >= 2) {
            Text(
                text = "$totalCount resultados",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }
        
        LazyColumn( 
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            items(
                count = results.size,
                key = { index -> results[index].id }
            ) { index ->
                val result = results[index]
                
                val context = LocalContext.current
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .combinedClickable(
                            onClick = {
                            // VALIDATION & LOGGING (FIX)
                            val bookStr = result.bookId
                            val chapter = result.chapter
                            val verse = result.verse
                            
                            android.util.Log.d("SearchClick", "Click: $bookStr $chapter:$verse")
                            
                            if (chapter > 0 && verse > 0) {
                                onNavigateToVerse(result)
                            } else {
                                android.util.Log.w("SearchClick", "Invalid data, ignoring click")
                            }
                        },
                        onLongClick = {
                            // Copy verse to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Versículo",
                                "${result.reference}\n${result.text}"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Versículo copiado", Toast.LENGTH_SHORT).show()
                        }
                    ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${result.reference}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = result.text,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            if (results.isEmpty() && !isSearching && query.length >= 2) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No se encontraron resultados", color = Color.Gray)
                            Text("Prueba con otra palabra o filtro", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
