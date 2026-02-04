package com.biblia.koine.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ReaderMenuDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onFontSettingsClick: () -> Unit,
    onConfigClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Fuentes") },
            onClick = {
                onFontSettingsClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.FontDownload, null)
            }
        )
        
        HorizontalDivider()
        
        DropdownMenuItem(
            text = { Text("Configuración") },
            onClick = {
                onConfigClick()
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Settings, null)
            }
        )
    }
}
