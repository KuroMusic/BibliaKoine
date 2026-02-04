package com.biblia.koine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.koine.viewmodel.LexiconViewModel
import com.biblia.koine.util.BibleCitationParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    topic: String,
    viewModel: LexiconViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val word by viewModel.selectedWord.collectAsState()
    val versePreview by viewModel.versePreview.collectAsState()
    val isShowingPreview by viewModel.isShowingPreview.collectAsState()
    val previewReference by viewModel.previewReference.collectAsState()
    
    val goldColor = Color(0xFFFFD700)
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(topic) {
        viewModel.loadWord(topic)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = word?.topic ?: topic,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = goldColor,
                    navigationIconContentColor = goldColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (word == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = goldColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "DEFINICIÓN",
                    style = MaterialTheme.typography.labelLarge,
                    color = goldColor,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = goldColor.copy(alpha = 0.3f))
                Spacer(Modifier.height(24.dp))

                val cleanDefinition = remember(word) {
                    word?.definition?.replace(Regex("\\{[^}]*\\}|\\\\[a-z0-9]+|\\\\'.."), "")?.trim() ?: ""
                }

                val annotatedText = remember(cleanDefinition) {
                    processCitations(cleanDefinition, goldColor)
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 22.sp, // Reduced slightly to match request exactly, but making it comfortable
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    ),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(tag = "CITATION", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val parts = annotation.item.split("|")
                                if (parts.size == 5) {
                                    val bookId = parts[0]
                                    val chapter = parts[2].toInt()
                                    val verse = parts[3].toInt()
                                    val rawText = parts[4]
                                    viewModel.fetchVersePreview(bookId, chapter, verse, rawText)
                                }
                            }
                    }
                )
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    if (isShowingPreview) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissPreview() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = previewReference,
                    style = MaterialTheme.typography.headlineSmall,
                    color = goldColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = versePreview ?: "Cargando...",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.dismissPreview() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

private fun processCitations(text: String, goldColor: Color): AnnotatedString {
    val citations = BibleCitationParser.findCitations(text)
    return buildAnnotatedString {
        var lastIndex = 0
        citations.forEach { citation ->
            // Text before citation
            val start = text.indexOf(citation.rawText, lastIndex)
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }
            
            // The citation itself
            pushStringAnnotation(
                tag = "CITATION",
                annotation = "${citation.bookId}|${citation.bookName}|${citation.chapter}|${citation.verse}|${citation.rawText}"
            )
            withStyle(style = SpanStyle(color = goldColor, fontWeight = FontWeight.Bold)) {
                append(citation.rawText)
            }
            pop()
            
            lastIndex = start + citation.rawText.length
        }
        
        // Final part
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
