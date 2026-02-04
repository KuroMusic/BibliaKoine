package com.biblia.koine.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.viewmodel.DisplayVerse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseContextMenu(
    verse: DisplayVerse, 
    onDismiss: () -> Unit,
    onAction: (VerseAction) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with Reference
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = verse.bookName.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${verse.chapter}:${verse.number}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
            
            // GROUP 1: PERSONAL ACTIONS
            MenuSectionHeader("ACCIONES")
            MenuOption(
                icon = Icons.Rounded.Palette,
                text = "Destacar",
                onClick = { 
                    onAction(VerseAction.Highlight(verse)) 
                    onDismiss()
                }
            )
            MenuOption(
                icon = Icons.Rounded.Note,
                text = if (verse.hasNote) "Ver/Editar nota" else "Agregar nota",
                onClick = { 
                    onAction(VerseAction.AddNote(verse)) 
                    onDismiss()
                }
            )
            MenuOption(
                icon = Icons.Rounded.Bookmark,
                text = if (verse.isBookmarked) "Quitar marcador" else "Guardar marcador",
                onClick = { 
                    onAction(VerseAction.Bookmark(verse)) 
                    onDismiss()
                }
            )

            // GROUP 2: SHARING
            MenuSectionHeader("COMPARTIR")
            MenuOption(
                icon = Icons.Rounded.ContentCopy,
                text = "Copiar texto",
                onClick = { 
                    onAction(VerseAction.Copy(verse)) 
                    onDismiss()
                }
            )
            MenuOption(
                icon = Icons.Rounded.Share,
                text = "Compartir versículo",
                onClick = { 
                    onAction(VerseAction.Share(verse)) 
                    onDismiss()
                }
            )
            MenuOption(
                icon = Icons.Rounded.Image,
                text = "Crear imagen de versículo",
                onClick = { 
                    onAction(VerseAction.CreateImage(verse)) 
                    onDismiss()
                }
            )

            // GROUP 3: STUDY
            MenuSectionHeader("ESTUDIO")
            MenuOption(
                icon = Icons.Rounded.School,
                text = "Estudiar Palabras (Léxico)",
                onClick = {
                    onAction(VerseAction.Study(verse))
                    onDismiss()
                }
            )
            
            val strongRegex = Regex("""[GH]\d+""")
            val firstStrongMatch = strongRegex.find(verse.text)
            
            if (firstStrongMatch != null) {
                MenuOption(
                    icon = Icons.Rounded.MenuBook,
                    text = "Ver definición Strong (${firstStrongMatch.value})",
                    onClick = {
                        onAction(VerseAction.LookupStrong(verse, firstStrongMatch.value))
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun MenuSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun MenuOption(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

