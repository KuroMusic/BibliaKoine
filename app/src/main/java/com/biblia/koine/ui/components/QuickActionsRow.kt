package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quick Action Buttons for Home Screen
 */
@Composable
fun QuickActionsRow(
    onRandomVerse: () -> Unit,
    onDailyPlan: () -> Unit,
    onFavorites: () -> Unit,
    goldColor: Color = Color(0xFFD4AF37)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Default.Shuffle,
            label = "Aleatorio",
            goldColor = goldColor,
            modifier = Modifier.weight(1f),
            onClick = onRandomVerse
        )
        QuickActionCard(
            icon = Icons.AutoMirrored.Filled.EventNote,
            label = "Plan",
            goldColor = goldColor,
            modifier = Modifier.weight(1f),
            onClick = onDailyPlan
        )
        QuickActionCard(
            icon = Icons.Default.Favorite,
            label = "Favoritos",
            goldColor = goldColor,
            modifier = Modifier.weight(1f),
            onClick = onFavorites
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    goldColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, // Requested change: text/icons black in light mode
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
