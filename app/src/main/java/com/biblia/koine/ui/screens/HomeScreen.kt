package com.biblia.koine.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.data.ReadingProgress
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.viewmodel.DisplayVerse
import com.biblia.koine.data.BibleBooksMetadata
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: BibleViewModel,
    onNavigateToReader: (String, Int) -> Unit
) {
    val verseOfDay by viewModel.verseOfDay.collectAsState()
    val progress by viewModel.readingProgress.collectAsState()
    
    val goldColor = MaterialTheme.colorScheme.primary
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp) // Generous spacing
    ) {
        // 1. Greeting Header
        item(key = "greeting") {
            GreetingHeader()
        }
        
        // 2. Continue Reading (Minimalist)
        item(key = "continue_reading") {
            if (progress != null) {
                MinimalistContinueReading(
                    progress = progress!!,
                    goldColor = goldColor,
                    onClick = { 
                        viewModel.navigateToVerseExact(progress!!.currentBookId, progress!!.currentChapter, progress!!.lastReadVerse)
                        onNavigateToReader(progress!!.currentBookId, progress!!.currentChapter)
                    }
                )
            }
        }
        
        // 3. About Section
        item(key = "about") {
            AboutBibliaKoine()
        }
        
        // 4. Daily Verse Card (NEW)
        item(key = "daily_verse") {
            DailyVerseCard(
                verse = verseOfDay,
                goldColor = goldColor,
                onClick = { verseOfDay?.let { onNavigateToReader(it.bookId, it.chapter) } }
            )
        }
        
        // 5. Word of the Day (NEW)
        item(key = "word_of_day") {
            WordOfTheDayCard(goldColor = goldColor)
        }
        
        // 6. Meditative Verse Card
        item(key = "verse_of_day") {
            MeditativeVerseCard(
                verse = verseOfDay,
                goldColor = goldColor,
                onClick = { verseOfDay?.let { onNavigateToReader(it.bookId, it.chapter) } }
            )
        }
        
        // 7. Spiritual Footer
        item(key = "footer") {
            SpiritualFooter()
        }
    }
}

@Composable
fun GreetingHeader() {
    val currentDate = remember { getCurrentDate() }
    val goldColor = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Biblia Koiné",
            style = MaterialTheme.typography.labelMedium,
            color = goldColor,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Hola, Dios te bendiga",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = currentDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

fun getCurrentDate(): String {
    return try {
        val formatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        formatter.format(Date())
    } catch (e: Exception) {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault()).format(Date())
    }
}

@Composable
fun MinimalistContinueReading(
    progress: ReadingProgress,
    goldColor: Color,
    onClick: () -> Unit
) {
    val bookName = BibleBooksMetadata.getName(progress.currentBookId)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "Retoma tu estudio en",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        Spacer(Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = bookName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${progress.currentChapter}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Thin minimalist progress line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((progress.lastReadVerse / 50f).coerceIn(0f, 1f))
                    .height(1.dp)
                    .background(goldColor)
            )
        }
    }
}

@Composable
fun AboutBibliaKoine() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Acerca de Biblia Koine",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "Biblia Koine fue diseñada para el estudio profundo de las Escrituras, integrando herramientas de concordancia y lenguajes originales para una comprensión fiel del mensaje divino.",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun MeditativeVerseCard(verse: DisplayVerse?, goldColor: Color, onClick: () -> Unit) {
    if (verse == null) return
    
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Subtle gradient background
    val gradientColors = if (isDarkTheme) {
        listOf(
            Color(0xFF2C2C2C),
            Color(0xFF1A1A1A)
        )
    } else {
        listOf(
            Color(0xFFFAF9F6),
            Color(0xFFF5F5F0)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                brush = Brush.verticalGradient(gradientColors),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\"${verse.text}\"",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                fontSize = 20.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = verse.reference,
                style = MaterialTheme.typography.bodyMedium,
                color = goldColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SpiritualFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\"Escudriñad las Escrituras...\" (Juan 5:39)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun DailyVerseCard(verse: DisplayVerse?, goldColor: Color, onClick: () -> Unit) {
    if (verse == null) return
    
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Subtle gradient for daily verse
    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))
    } else {
        listOf(Color(0xFFFAF9F6), Color(0xFFF5F5F0))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = verse.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 24.sp
                )
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = verse.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = goldColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WordOfTheDayCard(goldColor: Color) {
    val wordOfDay = remember { com.biblia.koine.data.GreekWordRepository.getWordOfDay() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tesoro del Idioma Original",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Greek word
            Text(
                text = wordOfDay.greek,
                style = MaterialTheme.typography.headlineMedium,
                color = goldColor,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(4.dp))
            
            // Transliteration
            Text(
                text = wordOfDay.transliteration,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Definition
            Text(
                text = wordOfDay.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
