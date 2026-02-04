package com.biblia.koine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.data.UserStats
import com.biblia.koine.ui.components.FontSettingsDialog

@Composable
fun ProfileScreen(viewModel: BibleViewModel) {
    val prefs by viewModel.userPrefs.collectAsState()
    val context = LocalContext.current
    
    val goldColor = Color(0xFFFFD700)
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showLineSpacingDialog by remember { mutableStateOf(false) }

    val backgroundColor = if (MaterialTheme.colorScheme.primary == goldColor) Color.Black else MaterialTheme.colorScheme.background

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Cabecera de Perfil Minimalista
        item {
            ProfileHeader(
                appName = "BibliaKoiné",
                tagline = "Tu compañera de estudio bíblico",
                goldColor = goldColor
            )
        }
        
        // ─── CONFIGURACIÓN DE ESTUDIO ───
        item { ConfigSectionTitle("ESTUDIO", goldColor) }
        item {
            ConfigCard {
                Column {
                    ConfigSwitchItem(
                        title = "Mostrar Números Strong",
                        icon = Icons.Default.MenuBook,
                        subtitle = "Ver códigos griegos y hebreos",
                        checked = prefs.enableStrongs,
                        onCheckedChange = { viewModel.toggleStrongsNumbers(it) },
                        goldColor = goldColor
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    ConfigSwitchItem(
                        title = "Palabras de Cristo en rojo",
                        icon = Icons.Default.AutoStories,
                        checked = prefs.showRedLetters,
                        onCheckedChange = { viewModel.toggleRedLetters(it) },
                        goldColor = goldColor
                    )
                }
            }
        }

        // ─── APARIENCIA Y PERSONALIZACIÓN ───
        item { ConfigSectionTitle("PERSONALIZACIÓN", goldColor) }
        item {
            ConfigCard {
                Column {
                    ConfigNavigationItem(
                        title = "Tema de la aplicación",
                        icon = Icons.Default.Palette,
                        valueText = when (prefs.theme) {
                            "claro" -> "Claro"
                            "oscuro" -> "Oscuro"
                            else -> "Sistema"
                        },
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    ConfigNavigationItem(
                        title = "Ajustes de fuente",
                        icon = Icons.Default.TextFields,
                        valueText = "${prefs.fontFamily} - ${prefs.fontSize} pt",
                        onClick = { showFontDialog = true }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    ConfigNavigationItem(
                        title = "Espacio entre líneas",
                        icon = Icons.Default.FormatLineSpacing,
                        valueText = "${String.format("%.1f", prefs.lineSpacing)}x",
                        onClick = { showLineSpacingDialog = true }
                    )
                }
            }
        }
        
        // ─── SOPORTE Y LEGAL ───
        item { ConfigSectionTitle("SOPORTE Y LEGAL", goldColor) }
        item {
            ConfigCard {
                Column {
                    ConfigNavigationItem(
                        title = "Contactar Soporte",
                        icon = Icons.Default.Help,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KuroMusic"))
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    ConfigNavigationItem(
                        title = "Política de Privacidad",
                        icon = Icons.Default.PrivacyTip,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KuroMusic/privacy"))
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    ConfigNavigationItem(
                        title = "Términos de Uso",
                        icon = Icons.Default.Gavel,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KuroMusic/terms"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        item {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Versión 1.2.0 • Hecho con amor en Koiné",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }

    // --- DIALOGS ---
    if (showFontDialog) {
        FontSettingsDialog(
            currentSize = prefs.fontSize,
            currentFont = prefs.fontFamily,
            onSizeChange = { viewModel.changeFontSize(it) },
            onFontChange = { viewModel.changeFontFamily(it) },
            onDismiss = { showFontDialog = false }
        )
    }

    if (showThemeDialog) {
        SimpleListDialog(
            title = "Seleccionar Tema",
            options = listOf("claro" to "Claro", "oscuro" to "Oscuro", "auto" to "Automático"),
            selectedValue = prefs.theme,
            onSelect = { 
                viewModel.changeTheme(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLineSpacingDialog) {
        SimpleListDialog(
            title = "Espacio entre líneas",
            options = listOf("1.0" to "1.0x", "1.2" to "1.2x", "1.4" to "1.4x", "1.6" to "1.6x", "1.8" to "1.8x", "2.0" to "2.0x"),
            selectedValue = prefs.lineSpacing.toString(),
            onSelect = { 
                viewModel.changeLineSpacing(it.toFloat())
                showLineSpacingDialog = false
            },
            onDismiss = { showLineSpacingDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(appName: String, tagline: String, goldColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = goldColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = goldColor
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            appName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            tagline,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ConfigSectionTitle(title: String, goldColor: Color) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = goldColor,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        letterSpacing = 2.sp
    )
}

@Composable
fun ConfigCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

@Composable
fun ConfigNavigationItem(
    title: String,
    icon: ImageVector,
    valueText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (valueText != null) {
                Text(valueText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun ConfigSwitchItem(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    goldColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = goldColor,
                checkedTrackColor = goldColor.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SimpleListDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (key, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedValue == key,
                            onClick = { onSelect(key) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

