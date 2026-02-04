package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * Reading Streak Calendar - Shows current month with highlighted days where user read the Bible
 */
@Composable
fun ReadingStreakCalendar(
    currentStreak: Int,
    readDays: Set<Long> = emptySet(), // Timestamps of days with reading activity
    goldColor: Color = Color(0xFFD4AF37)
) {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Get month data
    calendar.set(currentYear, currentMonth, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale("es", "ES"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    monthName.orEmpty().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = goldColor.copy(0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🔥",
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$currentStreak días",
                            color = goldColor, // Keep gold for streak count
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Day headers
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Calendar grid
            val totalCells = 42 // 6 weeks
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(totalCells) { index ->
                    val dayNumber = index - firstDayOfWeek + 1
                    val isValidDay = dayNumber in 1..daysInMonth
                    val isToday = isValidDay && dayNumber == currentDay
                    
                    // Check if this day was read
                    val isDayRead = if (isValidDay) {
                        val dayCalendar = Calendar.getInstance().apply {
                            set(currentYear, currentMonth, dayNumber, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        readDays.contains(dayCalendar.timeInMillis)
                    } else false
                    
                    CalendarDay(
                        day = if (isValidDay) dayNumber else null,
                        isToday = isToday,
                        hasActivity = isDayRead,
                        goldColor = goldColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int?,
    isToday: Boolean,
    hasActivity: Boolean,
    goldColor: Color
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isToday) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) // Gold/Primary for today
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .background(
                when {
                    hasActivity -> goldColor.copy(0.3f)
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Text(
                "$day",
                color = when {
                    hasActivity -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.onSurfaceVariant // Or Primary? User said "círculo del día actual debe ser primary". Text inside usually contrasts. Let's stick to onSurface unless selected. If isToday (circled), let's make it stand out or kept standard. User said: "El círculo del día actual debe ser MaterialTheme.colorScheme.primary". Text color: "Todos los textos... deben usar color = MaterialTheme.colorScheme.onSurfaceVariant". Implementation: border is primary, text is onSurfaceVariant.
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f)
                },
                fontSize = 14.sp,
                fontWeight = if (hasActivity || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
