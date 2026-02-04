package com.biblia.koine.data

data class ScriptureReference(
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val text: String = "Ver referencia" // Placeholder text
) {
    override fun toString(): String = "$bookId $chapter:$verse"
}

object CrossReferencesData {
    
    fun getReferences(bookId: String, chapter: Int, verse: Int): List<ScriptureReference> {
        // Normalize Key: "Jhn 3:16"
        val key = "$bookId $chapter:$verse"
        return staticMap[key] ?: emptyList() 
    }

    private val staticMap = mapOf(
        // Genesis 1:1
        "Gen 1:1" to listOf(
             ScriptureReference("Jhn", 1, 1),
             ScriptureReference("Heb", 11, 3),
             ScriptureReference("Psa", 33, 6)
        ),
        // John 3:16
        "Jhn 3:16" to listOf(
             ScriptureReference("Num", 21, 9),
             ScriptureReference("Rom", 5, 8),
             ScriptureReference("1Jo", 4, 9)
        ),
        // Psalm 23:1
        "Psa 23:1" to listOf(
             ScriptureReference("Isa", 40, 11),
             ScriptureReference("Jer", 23, 4),
             ScriptureReference("Eze", 34, 11),
             ScriptureReference("Jhn", 10, 11),
             ScriptureReference("1Pe", 2, 25)
        ),
        // Romans 8:28
        "Rom 8:28" to listOf(
             ScriptureReference("Gen", 50, 20),
             ScriptureReference("Jer", 29, 11),
             ScriptureReference("2Ti", 1, 9)
        ),
        // Philippians 4:13
        "Php 4:13" to listOf(
             ScriptureReference("Jhn", 15, 5),
             ScriptureReference("2Co", 12, 9),
             ScriptureReference("Eph", 3, 16)
        ),
         // Jer 29:11
        "Jer 29:11" to listOf(
             ScriptureReference("Psa", 40, 5),
             ScriptureReference("Isa", 55, 12)
        )
    )
}
