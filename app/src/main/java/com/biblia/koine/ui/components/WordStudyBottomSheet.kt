package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.data.room.StrongDefinition

/**
 * Word Study Bottom Sheet - Tap any word to see:
 * - Strong's Number (#G25)
 * - Original language (λόγος - logos)
 * - Definition
 * - Morphology (optional)
 * - Link to Concordance (all verses with this word)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordStudyBottomSheet(
    word: String,
    strongDefinition: StrongDefinition?,
    onDismiss: () -> Unit,
    onViewConcordance: (String) -> Unit = {}
) {
    val goldColor = Color(0xFFD4AF37)
    val context = androidx.compose.ui.platform.LocalContext.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        tonalElevation = 16.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Estudio de Palabra",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.Gray)
                    }
                }
            }
            
            // Selected Word
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Palabra seleccionada",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            word,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            if (strongDefinition != null) {
                // Strong's Number
                item {
                    InfoSection(
                        icon = Icons.Default.Tag,
                        title = "Número de Strong",
                        content = strongDefinition.topic,
                        goldColor = goldColor
                    )
                }
                
                // Definition
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Article,
                                    contentDescription = null,
                                    tint = goldColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Definición",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = goldColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                strongDefinition.definition ?: "Sin definición",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                
                // Concordance Button
                item {
                    Button(
                        onClick = { onViewConcordance(word) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = goldColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ver todas las referencias",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // No Strong's data found
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No se encontró información de Strong para esta palabra",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // External Options
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/search?q=$word+biblia"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Google", color = Color.White)
                    }
                    
                    Button(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://es.wikipedia.org/wiki/$word"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Wikipedia", color = Color.White)
                    }
                }
            }

            // Bottom Spacing
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun InfoSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    goldColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = goldColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    color = goldColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}
