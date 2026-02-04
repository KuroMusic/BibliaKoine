package com.biblia.koine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun FontSettingsDialog(
    currentSize: Int,
    currentFont: String,
    onSizeChange: (Int) -> Unit,
    onFontChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val goldColor = MaterialTheme.colorScheme.primary
    // fontSizes removed
    val fontFamilies = listOf("SansSerif", "Serif", "Monospace")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Ajustes de Texto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "TAMAÑO DE FUENTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = goldColor
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (currentSize > 12) onSizeChange(currentSize - 2) }) {
                        Text("A-", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    }
                    
                    Text("$currentSize", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
                    
                    IconButton(onClick = { if (currentSize < 36) onSizeChange(currentSize + 2) }) {
                        Text("A+", color = goldColor, fontSize = 24.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "ESTILOS DE FUENTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = goldColor
                )
                
                Spacer(Modifier.height(8.dp))
                
                fontFamilies.forEach { family ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFontChange(family) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         RadioButton(
                            selected = currentFont == family,
                            onClick = { onFontChange(family) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = goldColor,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(family, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("LISTO", color = goldColor)
                }
            }
        }
    }
}
