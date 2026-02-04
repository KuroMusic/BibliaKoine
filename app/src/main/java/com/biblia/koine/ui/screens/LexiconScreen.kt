package com.biblia.koine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.koine.data.room.StrongDefinition
import com.biblia.koine.viewmodel.LexiconViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexiconScreen(
    lexiconViewModel: LexiconViewModel = viewModel(),
    initialQuery: String? = null,
    onNavigateToWord: (String) -> Unit = {}
) {
    LaunchedEffect(initialQuery) {
        if (initialQuery != null) {
            lexiconViewModel.onSearchQueryChanged(initialQuery)
        }
    }
    val searchQuery by lexiconViewModel.searchQuery.collectAsState()
    val searchResults by lexiconViewModel.searchResults.collectAsState()
    val isLoading by lexiconViewModel.isLoading.collectAsState()
    
    val goldColor = Color(0xFFFFD700) // Corrected Vine Gold
    
    // Auto-navigate if single result when searching from outside
    LaunchedEffect(searchResults) {
        if (initialQuery != null && searchResults.size == 1) {
            onNavigateToWord(searchResults.first().topic)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Diccionario Teológico",
                style = MaterialTheme.typography.headlineMedium,
                color = goldColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { lexiconViewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar palabra en el diccionario...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = goldColor) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { lexiconViewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLeadingIconColor = goldColor,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Results
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = goldColor)
            }
        } else if (searchResults.isEmpty() && searchQuery.length >= 2) {
             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron resultados", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (searchQuery.length < 2) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Book, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("Ingresa al menos 2 caracteres", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(searchResults) { result ->
                    LexiconItem(
                        item = result,
                        goldColor = goldColor,
                        onClick = {
                            onNavigateToWord(result.topic)
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun LexiconItem(
    item: StrongDefinition,
    goldColor: Color,
    onClick: () -> Unit
) {
    val language = when {
        item.topic.startsWith("G") -> "Griego"
        item.topic.startsWith("H") -> "Hebreo"
        else -> ""
    }
    
    // Si topic es un código, intentamos sacar la palabra de la definición
    val displayTitle = remember(item) {
        val cleaned = getCleanPreview(item.definition ?: "")
        if (item.topic.matches(Regex("^[GH][0-9].*"))) {
            // Extraer la primera palabra o frase antes de un punto o coma
            cleaned.split(Regex("[.,;]")).firstOrNull()?.trim() ?: item.topic
        } else {
            item.topic
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.topic,
                    color = goldColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            val previewText = getCleanPreview(item.definition ?: "")
            
            Text(
                text = previewText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Limpieza mejorada de códigos RTF y etiquetas de la definición
 */
fun getCleanPreview(text: String): String {
   return text.replace(Regex("\\{[^}]*\\}|\\\\[a-z0-9]+|\\\\'.."), "").trim()
}

