package com.biblia.koine.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.viewmodel.DisplayVerse

@Composable
fun ColorPickerOverlay(
    verse: DisplayVerse,
    currentColor: Color?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onRemoveHighlight: () -> Unit
) {
    val highlightColors = listOf(
        Color(0xFFFFF176) to "Amarillo",
        Color(0xFF81C784) to "Verde",
        Color(0xFF64B5F6) to "Azul",
        Color(0xFFFFB74D) to "Naranja",
        Color(0xFFF06292) to "Rosa",
        Color(0xFFBA68C8) to "Morado",
        Color(0xFFE57373) to "Rojo",
        Color(0xFF4DD0E1) to "Cian"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { } // Prevent dismiss on content click
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = Color(0xFF1E1E1E),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Destacar Versículo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = verse.reference,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Color Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        highlightColors.take(4).forEach { (color, _) ->
                            ColorOptionCircle(
                                color = color,
                                isSelected = currentColor == color,
                                onClick = {
                                    onColorSelected(color)
                                    onDismiss()
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        highlightColors.drop(4).forEach { (color, _) ->
                            ColorOptionCircle(
                                color = color,
                                isSelected = currentColor == color,
                                onClick = {
                                    onColorSelected(color)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentColor != null) {
                        TextButton(
                            onClick = {
                                onRemoveHighlight()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5722))
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Quitar Destacado")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(48.dp)) // Maintain consistency
                    }
                }
            }
        }
    }
}

@Composable
fun ColorOptionCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}
