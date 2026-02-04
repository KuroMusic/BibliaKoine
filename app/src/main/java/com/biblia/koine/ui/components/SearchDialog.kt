package com.biblia.koine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SearchDialog(
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val goldColor = Color(0xFFD4AF37)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Buscar en la Biblia",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                Spacer(Modifier.height(20.dp))
                
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Escribe una palabra o frase...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Black,
                        unfocusedContainerColor = Color.Black,
                        focusedIndicatorColor = goldColor,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = goldColor)
                    }
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (query.isNotBlank()) {
                                onSearch(query)
                                onDismiss()
                            }
                        },
                        enabled = query.length >= 3,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = goldColor,
                            disabledContainerColor = goldColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Text("BUSCAR", color = Color.Black)
                    }
                }
            }
        }
    }
}
