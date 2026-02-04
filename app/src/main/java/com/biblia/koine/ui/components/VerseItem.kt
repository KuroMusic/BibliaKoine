package com.biblia.koine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.biblia.koine.viewmodel.DisplayVerse
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.data.prefs.UserPreferences

sealed class VerseAction {
    data class Highlight(val verse: DisplayVerse) : VerseAction()
    data class AddNote(val verse: DisplayVerse) : VerseAction()
    data class Bookmark(val verse: DisplayVerse) : VerseAction()
    data class Share(val verse: DisplayVerse) : VerseAction()
    data class Copy(val verse: DisplayVerse) : VerseAction()
    data class CreateImage(val verse: DisplayVerse) : VerseAction()
    data class LookupStrong(val verse: DisplayVerse, val strongNumber: String) : VerseAction()
    data class Study(val verse: DisplayVerse) : VerseAction() // NEW: Open word selector
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VerseItem(
    verse: DisplayVerse, 
    viewModel: BibleViewModel,
    prefs: UserPreferences.Prefs, // NEW: Pass explicit prefs
    showNumbers: Boolean,
    showSectionTitles: Boolean, 
    showRedLetters: Boolean,
    showStrongs: Boolean = false,
    // Remove explicit fontSize/lineSpacing args as they come from prefs now
    justify: Boolean = false,
    shouldHighlight: Boolean = false, // From search/scroll focus
    onAction: (VerseAction) -> Unit    
) {
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedVerses by viewModel.selectedVerses.collectAsState()
    val isSelected = selectedVerses.contains(verse.number)
    
    
    // CRITICAL FIX: Use MaterialTheme.colorScheme.surface as base
    // Highlight uses ALPHA BLENDING so text remains readable
    val targetBackgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer // Dynamic Selection Color
        verse.highlightColor != null -> verse.highlightColor!!.copy(alpha = 0.3f) // 30% Alpha Overlay for Highlights
        shouldHighlight -> Color(0xFFFFD700).copy(alpha = 0.5f) // GOLDEN Flash for search navigation
        else -> MaterialTheme.colorScheme.background // Was Surface, changing to background for cleaner look on list
    }
    
    // Animate background color for smooth transitions
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
        label = "verseBackgroundColor"
    )

    val goldColor = MaterialTheme.colorScheme.primary
    
    // Calculate content color based on theme, NOT background (since bg is transparent/alpha)
    // For selected, use onPrimaryContainer. For others, onBackground.
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onBackground
    }

    var showMenu by remember { mutableStateOf(false) }

    // Fix: Use Row + Modifier.background for reliable highlight rendering
    // This avoids Card containerColor caching issues and provides cleaner flat look
    
    // Container for the item (Card-based as requested for persistence/look)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = {
                    if (selectionMode) {
                        viewModel.toggleVerseSelection(verse.number)
                    } else {
                        showMenu = true
                    }
                },
                onLongClick = {
                    viewModel.enterSelectionMode()
                    viewModel.toggleVerseSelection(verse.number)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (showSectionTitles && !verse.heading.isNullOrBlank()) {
                Text(
                    text = verse.heading!!,
                    style = MaterialTheme.typography.titleMedium,
                    color = goldColor,
                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(verticalAlignment = Alignment.Top) {
                if (showNumbers) {
                    Text(
                        text = verse.number.toString(),
                        color = if (isSelected) contentColor else goldColor,
                        fontSize = (prefs.fontSize * 0.7f).toFloat().sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                    )
                }

                val annotatedText = remember(verse.text, showStrongs) {
                    buildAnnotatedString {
                        val strongRegex = Regex("([GH]\\d+)")
                        val matches = strongRegex.findAll(verse.text)
                        
                        var lastIndex = 0
                        for (match in matches) {
                            append(verse.text.substring(lastIndex, match.range.first))
                            
                            if (showStrongs) {
                                pushStringAnnotation(tag = "STRONG", annotation = match.value)
                                withStyle(style = SpanStyle(
                                    color = Color(0xFFFFD700).copy(alpha = 0.5f), // Gold with opacity
                                    fontSize = 10.sp,
                                    baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript
                                )) {
                                    append(match.value)
                                }
                                pop()
                            }
                            lastIndex = match.range.last + 1
                        }
                        append(verse.text.substring(lastIndex))
                    }
                }

                var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                
                Text(
                    text = annotatedText,
                    onTextLayout = { textLayoutResult = it },
                    style = TextStyle(
                        color = when {
                            verse.isRed -> Color(0xFFD32F2F)
                            else -> contentColor
                        },
                        fontSize = prefs.fontSize.sp,
                        lineHeight = (prefs.fontSize * prefs.lineSpacing).toFloat().sp,
                        textAlign = if (justify) TextAlign.Justify else TextAlign.Start
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(selectionMode, showStrongs) {
                            detectTapGestures(
                                onTap = { offset ->
                                    var handled = false
                                    if (showStrongs) {
                                        textLayoutResult?.let { layoutResult ->
                                            val position = layoutResult.getOffsetForPosition(offset)
                                            annotatedText.getStringAnnotations(tag = "STRONG", start = position, end = position)
                                                .firstOrNull()?.let { annotation ->
                                                    onAction(VerseAction.LookupStrong(verse, annotation.item))
                                                    handled = true
                                                }
                                        }
                                    }
                                    
                                    if (!handled) {
                                        if (selectionMode) {
                                            viewModel.toggleVerseSelection(verse.number)
                                        } else {
                                            showMenu = true
                                        }
                                    }
                                },
                                onLongPress = {
                                    viewModel.enterSelectionMode()
                                    viewModel.toggleVerseSelection(verse.number)
                                }
                            )
                        }
                )

                // Highlight Button
                if (!selectionMode) {
                    IconButton(
                        onClick = { onAction(VerseAction.Highlight(verse)) },
                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Resaltar",
                            tint = if (verse.highlightColor != null) contentColor.copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Icons row
            if (verse.hasNote || verse.isBookmarked) {
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    if (verse.hasNote) Icon(Icons.Rounded.Description, null, modifier = Modifier.size(12.dp), tint = goldColor)
                    if (verse.isBookmarked) Icon(Icons.Rounded.Bookmark, null, modifier = Modifier.size(12.dp), tint = goldColor)
                }
            }
        }
    }

    if (showMenu) {
        VerseContextMenu(
            verse = verse,
            onDismiss = { showMenu = false },
            onAction = onAction
        )
    }
}

