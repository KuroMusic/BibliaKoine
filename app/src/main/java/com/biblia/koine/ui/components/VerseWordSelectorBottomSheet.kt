package com.biblia.koine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.viewmodel.DisplayVerse

/**
 * Bottom Sheet to select a word from the verse for study
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseWordSelectorBottomSheet(
    verse: DisplayVerse,
    onDismiss: () -> Unit,
    onWordSelected: (String) -> Unit
) {
    val goldColor = Color(0xFFD4AF37)
    
    // Split verse into words, removing punctuation
    val words = remember(verse.text) {
        verse.text.split(Regex("[\\s,.;:?!\"()]+"))
            .filter { it.length > 2 } // Filter out very short words
            .distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = "Selecciona una palabra",
                    style = MaterialTheme.typography.titleLarge,
                    color = goldColor,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            androidx.compose.material3.Text(
                text = verse.text, // Show full text for context
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            HorizontalDivider(color = Color.DarkGray)
            Spacer(Modifier.height(16.dp))

            // Helper text
            androidx.compose.material3.Text(
                text = "Toca para estudiar:",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(words) { word ->
                    SuggestionChip(
                        onClick = { 
                            onWordSelected(word)
                            onDismiss()
                        },
                        label = { 
                            androidx.compose.material3.Text(
                                text = word, 
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ) 
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF2A2A2A),
                            labelColor = Color.White
                        ),
                        // Fixed type mismatch: Use BorderStroke directly or null
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
