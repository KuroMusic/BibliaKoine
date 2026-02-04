package com.biblia.koine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BibleVersionSelector(
    currentVersion: String,
    onVersionSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val goldColor = Color(0xFFD4AF37)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Seleccionar Versión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Español
                Text(
                    "ESPAÑOL",
                    style = MaterialTheme.typography.labelMedium,
                    color = goldColor
                )
                
                VersionItem(
                    name = "Reina Valera 1960",
                    code = "RV1960",
                    isSelected = currentVersion == "RV1960",
                    goldColor = goldColor,
                    onClick = { onVersionSelect("RV1960") }
                )
                
                VersionItem(
                    name = "Reina Valera 1909",
                    code = "RV1909",
                    isSelected = false,
                    goldColor = goldColor,
                    onClick = { /* TODO */ },
                    isDownloadable = true
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Inglés
                Text(
                    "ENGLISH",
                    style = MaterialTheme.typography.labelMedium,
                    color = goldColor
                )
                
                VersionItem(
                    name = "King James Version",
                    code = "KJV",
                    isSelected = false,
                    goldColor = goldColor,
                    onClick = { /* TODO */ },
                    isDownloadable = true
                )
                
                VersionItem(
                    name = "New International Version",
                    code = "NIV",
                    isSelected = false,
                    goldColor = goldColor,
                    onClick = { /* TODO */ },
                    isDownloadable = true
                )
                
                Spacer(Modifier.height(24.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("CERRAR", color = goldColor)
                }
            }
        }
    }
}

@Composable
fun VersionItem(
    name: String,
    code: String,
    isSelected: Boolean,
    goldColor: Color,
    onClick: () -> Unit,
    isDownloadable: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = goldColor,
                unselectedColor = Color.Gray
            )
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(
                code,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        if (isDownloadable) {
            Icon(
                Icons.Default.CloudDownload,
                contentDescription = "Descargar",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
