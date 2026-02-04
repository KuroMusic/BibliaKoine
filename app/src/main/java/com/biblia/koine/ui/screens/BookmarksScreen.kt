package com.biblia.koine.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.biblia.koine.data.Bookmark
import com.biblia.koine.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(viewModel: BibleViewModel, onNavigateToVerse: (Bookmark) -> Unit = {}) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    
    // BibliaGold
    val goldColor = Color(0xFFD4AF37)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marcadores", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BookmarkBorder, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes marcadores aún", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(bookmarks) { bookmark ->
                    BookmarkCard(
                        bookmark = bookmark,
                        goldColor = goldColor,
                        onClick = { onNavigateToVerse(bookmark) },
                        onDelete = { viewModel.deleteBookmark(bookmark) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarkCard(
    bookmark: Bookmark,
    goldColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Bookmark,
                null,
                tint = goldColor
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${bookmark.bookId} ${bookmark.chapter}:${bookmark.verse}",
                    style = MaterialTheme.typography.titleSmall,
                    color = goldColor
                )
                Text(
                    bookmark.verseText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.LightGray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color.Gray)
            }
        }
    }
}
