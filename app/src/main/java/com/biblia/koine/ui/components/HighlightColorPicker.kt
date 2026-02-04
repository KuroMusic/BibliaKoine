package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.biblia.koine.viewmodel.DisplayVerse

@Composable
fun HighlightColorPicker(
    verse: DisplayVerse,
    onColorSelect: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color(0xFFFFEB3B) to "Amarillo",
        Color(0xFFFF9800) to "Naranja",
        Color(0xFFF44336) to "Rojo",
        Color(0xFF9C27B0) to "Morado",
        Color(0xFF2196F3) to "Azul",
        Color(0xFF4CAF50) to "Verde"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Destacar ${verse.reference}")
                Spacer(Modifier.height(16.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(colors) { (color, name) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { 
                                        onColorSelect(color)
                                        onDismiss()
                                    }
                            )
                            Text(name, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
