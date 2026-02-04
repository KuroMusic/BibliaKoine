package com.biblia.koine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblia.koine.viewmodel.DisplayVerse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorDialog(
    verse: DisplayVerse,
    existingNote: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf(existingNote) }
    
    val scrollState = rememberScrollState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        windowInsets = WindowInsets.ime // This helps the sheet resize for the keyboard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding() // Extra padding for the keyboard
                .verticalScroll(scrollState) // Allow scrolling if keyboard takes too much space
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nota del Versículo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = verse.reference,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Verse Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                )
            ) {
                Text(
                    text = "\"${verse.text}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Note Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 250.dp), // Dynamic height
                placeholder = {
                    Text(
                        "Escribe tu nota aquí...",
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFD4AF37),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFFD4AF37)
                ),
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onSave(noteText)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color.Black
                    ),
                    enabled = noteText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
