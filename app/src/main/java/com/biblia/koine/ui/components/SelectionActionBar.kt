package com.biblia.koine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SelectionActionBar(
    selectedCount: Int,
    onHighlight: () -> Unit,
    onBookmark: () -> Unit, // NEW
    onNote: () -> Unit, // NEW
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onImageShare: () -> Unit, 
    onStudy: () -> Unit, // NEW
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1E1E),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, "Cancelar", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "$selectedCount seleccionado${if (selectedCount > 1) "s" else ""}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row {
                IconButton(onClick = onHighlight) {
                    Icon(Icons.Rounded.Brush, "Destacar", tint = Color(0xFFD4AF37))
                }
                IconButton(onClick = onBookmark) {
                    Icon(Icons.Rounded.Bookmark, "Marcador", tint = Color.White)
                }
                IconButton(onClick = onNote) {
                    Icon(Icons.Rounded.NoteAdd, "Nota", tint = Color.White)
                }
                IconButton(onClick = onStudy) {
                    Icon(Icons.Rounded.School, "Estudiar", tint = Color.White)
                }
                IconButton(onClick = onImageShare) { 
                    Icon(Icons.Rounded.Image, "Imagen", tint = Color.White)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Rounded.ContentCopy, "Copiar", tint = Color.White)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Rounded.Share, "Compartir", tint = Color.White)
                }
            }
        }
    }
}
