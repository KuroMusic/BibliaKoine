package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ChapterSelectorDialog(
    // currentBook removed
    currentChapter: Int,
    totalChapters: Int,
    onChapterSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val goldColor = Color(0xFFD4AF37)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Seleccionar Capítulo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..totalChapters).toList()) { chapterNum ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (chapterNum == currentChapter) goldColor
                                    else Color.DarkGray
                                )
                                .clickable {
                                    onChapterSelect(chapterNum)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$chapterNum",
                                color = if (chapterNum == currentChapter) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("CERRAR", color = goldColor)
                }
            }
        }
    }
}
