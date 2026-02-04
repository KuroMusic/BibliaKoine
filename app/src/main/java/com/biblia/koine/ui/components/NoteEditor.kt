package com.biblia.koine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.biblia.koine.viewmodel.DisplayVerse

@Composable
fun NoteEditorDialog(
    verse: DisplayVerse,
    existingNote: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(existingNote ?: "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Nota en ${verse.reference}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "\"${verse.text}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Escribe tu nota aquí...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            onSave(noteText)
                            onDismiss()
                        },
                        enabled = noteText.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
