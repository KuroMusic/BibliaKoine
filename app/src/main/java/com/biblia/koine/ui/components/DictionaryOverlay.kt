package com.biblia.koine.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.data.room.StrongDefinition
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DictionaryOverlay(
    strongNumber: String?,
    isVisible: Boolean,
    definition: StrongDefinition?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible && strongNumber != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .clickable(enabled = false) { }, // Prevent dismiss on content click
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xFF1E1E1E),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Diccionario Strong",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Griego → Español",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFFD4AF37)
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "Cargando definición...",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            
                            definition != null -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Strong Number
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2A2A2A)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = Color(0xFFD4AF37),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "G${definition.topic}",
                                                    modifier = Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 8.dp
                                                    ),
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                "Número Strong",
                                                color = Color.LightGray,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    
                                    // Definition
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2A2A2A)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                "Definición en Español",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.Gray
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                definition.definition ?: "Sin definición",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.White,
                                                lineHeight = 26.sp
                                            )
                                        }
                                    }
                                }
                            }
                            
                            else -> {
                                // Not found
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "❌",
                                        fontSize = 48.sp
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Definición no encontrada",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "El número Strong #$strongNumber no está disponible",
                                        color = Color.DarkGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
