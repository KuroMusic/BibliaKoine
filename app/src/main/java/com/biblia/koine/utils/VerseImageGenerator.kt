package com.biblia.koine.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.biblia.koine.viewmodel.DisplayVerse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class VerseImageGenerator(private val context: Context) {
    
    suspend fun generateVerseCard(
        verse: DisplayVerse,
        template: CardTemplate = CardTemplate.MINIMALIST
    ): Bitmap = withContext(Dispatchers.Default) {
        
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background gradient based on template
        val gradient = when (template) {
            CardTemplate.MINIMALIST -> LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#1a237e"), Color.parseColor("#0d47a1")),
                null, Shader.TileMode.CLAMP
            )
            CardTemplate.ELEGANT -> LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#212121"), Color.parseColor("#424242")),
                null, Shader.TileMode.CLAMP
            )
            CardTemplate.SUNSET -> LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#ff6f00"), Color.parseColor("#f57c00")),
                null, Shader.TileMode.CLAMP
            )
            CardTemplate.NATURE -> LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#1b5e20"), Color.parseColor("#388e3c")),
                null, Shader.TileMode.CLAMP
            )
            CardTemplate.GOLD -> LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#7D5A29"), Color.parseColor("#D4AF37")),
                null, Shader.TileMode.CLAMP
            )
        }
        
        val backgroundPaint = Paint().apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Verse text configuration
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // Draw wrapped verse text
        val maxWidth = width * 0.85f
        val centerY = height / 2f
        drawWrappedText(canvas, verse.text, textPaint, maxWidth, centerY)
        
        // Reference text
        val refPaint = Paint().apply {
            color = Color.WHITE
            alpha = 200
            textSize = 44f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText(
            verse.reference,
            width / 2f,
            height * 0.85f,
            refPaint
        )
        
        // App branding
        val brandPaint = Paint().apply {
            color = Color.parseColor("#D4AF37") // BibliaKoine gold
            textSize = 32f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText(
            "BibliaKoine",
            width / 2f,
            height * 0.95f,
            brandPaint
        )
        
        bitmap
    }
    
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        maxWidth: Float,
        centerY: Float
    ) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        // Build lines by measuring text width
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        // Draw centered lines
        val lineHeight = paint.textSize * 1.5f
        val totalHeight = lines.size * lineHeight
        var y = centerY - (totalHeight / 2f)
        
        for (line in lines) {
            canvas.drawText(line, canvas.width / 2f, y, paint)
            y += lineHeight
        }
    }
    
    suspend fun shareVerseImage(bitmap: Bitmap, verse: DisplayVerse) {
        withContext(Dispatchers.IO) {
            // Save bitmap to cache directory
            val file = File(context.cacheDir, "verse_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            
            // Create FileProvider URI for sharing
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Build share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "${verse.text}\n\n${verse.reference}\n\nCompartido desde BibliaKoine")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Launch share sheet
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "Compartir Versículo"))
            }
        }
    }
}

enum class CardTemplate {
    MINIMALIST,
    ELEGANT,
    SUNSET,
    NATURE,
    GOLD
}
