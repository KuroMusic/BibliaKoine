package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightPickerBottomSheet(
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    // Vibrant colors requested by user
    val colors = listOf(
        Pair("#FFFF00", "Amarillo"),
        Pair("#00FF00", "Verde"),
        Pair("#0066FF", "Azul"),
        Pair("#FF69B4", "Rosa"),
        Pair("#FF8C00", "Naranja"),
        Pair("#00FFFF", "Cian")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecciona un color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // User Requested "Quitar resaltado" button
                IconButton(
                    onClick = { onColorSelected("REMOVE") },
                    modifier = Modifier.size(48.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.HighlightOff,
                        contentDescription = "Quitar resaltado",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                colors.forEach { (hex, _) ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Yellow }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(hex) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
