package com.biblia.koine.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.koine.utils.CardTemplate
import com.biblia.koine.utils.VerseImageGenerator
import com.biblia.koine.viewmodel.DisplayVerse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VerseImageDialog(
    verse: DisplayVerse,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val generator = remember { VerseImageGenerator(context) }
    var selectedTemplate by remember { mutableStateOf(CardTemplate.MINIMALIST) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    
    // Generate image when template changes
    LaunchedEffect(selectedTemplate, isVisible) {
        if (isVisible) {
            isGenerating = true
            generatedBitmap = generator.generateVerseCard(verse, selectedTemplate)
            isGenerating = false
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.9f))
                .clickable { /* Prevent clicks from dismissing */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Crear Imagen",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Image preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color(0xFFD4AF37))
                        } else {
                            generatedBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Vista previa del versículo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Template selector
                Text(
                    "Elige un diseño",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Template chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TemplateChip(
                        name = "Minimalista",
                        template = CardTemplate.MINIMALIST,
                        color = Color(0xFF0d47a1),
                        isSelected = selectedTemplate == CardTemplate.MINIMALIST,
                        onClick = { selectedTemplate = CardTemplate.MINIMALIST }
                    )
                    TemplateChip(
                        name = "Elegante",
                        template = CardTemplate.ELEGANT,
                        color = Color(0xFF424242),
                        isSelected = selectedTemplate == CardTemplate.ELEGANT,
                        onClick = { selectedTemplate = CardTemplate.ELEGANT }
                    )
                    TemplateChip(
                        name = "Atardecer",
                        template = CardTemplate.SUNSET,
                        color = Color(0xFFf57c00),
                        isSelected = selectedTemplate == CardTemplate.SUNSET,
                        onClick = { selectedTemplate = CardTemplate.SUNSET }
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TemplateChip(
                        name = "Naturaleza",
                        template = CardTemplate.NATURE,
                        color = Color(0xFF388e3c),
                        isSelected = selectedTemplate == CardTemplate.NATURE,
                        onClick = { selectedTemplate = CardTemplate.NATURE }
                    )
                    TemplateChip(
                        name = "Oro",
                        template = CardTemplate.GOLD,
                        color = Color(0xFFD4AF37),
                        isSelected = selectedTemplate == CardTemplate.GOLD,
                        onClick = { selectedTemplate = CardTemplate.GOLD }
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Share button
                Button(
                    onClick = {
                        generatedBitmap?.let { bitmap ->
                            CoroutineScope(Dispatchers.Main).launch {
                                generator.shareVerseImage(bitmap, verse)
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isGenerating
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Compartir Imagen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateChip(
    name: String,
    template: CardTemplate,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
