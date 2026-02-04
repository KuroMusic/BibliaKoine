package com.biblia.koine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.data.BibleBooksMetadata
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.viewmodel.SearchResult

/**
 * Concordance Screen - Shows all verses containing a specific word
 * Grouped by Testament and Book for easy navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcordanceScreen(
    word: String,
    viewModel: BibleViewModel,
    onNavigateToVerse: (SearchResult) -> Unit,
    onNavigateBack: () -> Unit
) {
    val goldColor = Color(0xFFD4AF37)
    
    // Search for the word
    LaunchedEffect(word) {
        if (word.isNotBlank()) {
            viewModel.search(word)
        }
    }
    
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    // Group results by Testament
    val oldTestament = remember(results) {
        results.filter {
            val bookNum = BibleBooksMetadata.getNumber(it.bookId)
            bookNum < 40
        }
    }
    
    val newTestament = remember(results) {
        results.filter {
            val bookNum = BibleBooksMetadata.getNumber(it.bookId)
            bookNum >= 40
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Concordancia: \"$word\"", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = goldColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        when {
            isSearching -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = goldColor)
                }
            }
            
            results.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No se encontraron referencias",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Summary
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = goldColor.copy(0.2f))
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = goldColor)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${results.size} referencias encontradas",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Old Testament
                    if (oldTestament.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "ANTIGUO TESTAMENTO (${oldTestament.size})",
                                color = goldColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        items(oldTestament, key = { "${it.bookId}_${it.chapter}_${it.verse}" }) { result ->
                            ConcordanceResultCard(result, word, goldColor, onNavigateToVerse)
                        }
                    }
                    
                    // New Testament
                    if (newTestament.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "NUEVO TESTAMENTO (${newTestament.size})",
                                color = goldColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        items(newTestament, key = { "${it.bookId}_${it.chapter}_${it.verse}" }) { result ->
                            ConcordanceResultCard(result, word, goldColor, onNavigateToVerse)
                        }
                    }
                    
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ConcordanceResultCard(
    result: SearchResult,
    searchWord: String,
    goldColor: Color,
    onClick: (SearchResult) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(result) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                result.reference,
                style = MaterialTheme.typography.labelLarge,
                color = goldColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                result.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                lineHeight = 20.sp
            )
        }
    }
}
