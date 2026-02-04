package com.biblia.koine.util

import com.biblia.koine.data.bibleBooks

data class ParsedCitation(
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int?,
    val rawText: String
)

object BibleCitationParser {
    // Mapping of common Spanish abbreviations/names to internal Book IDs
    private val bookNameMap = mapOf(
        "Gén" to "Gen", "Gen" to "Gen", "Génesis" to "Gen",
        "Éxo" to "Exo", "Exo" to "Exo", "Éxodo" to "Exo",
        "Lev" to "Lev", "Levítico" to "Lev",
        "Núm" to "Num", "Num" to "Num", "Números" to "Num",
        "Deu" to "Deu", "Deut" to "Deu", "Deuteronomio" to "Deu",
        "Jos" to "Jos", "Josué" to "Jos",
        "Jue" to "Jdg", "Jueces" to "Jdg",
        "Rut" to "Rut",
        "1Sa" to "1Sa", "1 Sa" to "1Sa", "1 Samuel" to "1Sa",
        "2Sa" to "2Sa", "2 Sa" to "2Sa", "2 Samuel" to "2Sa",
        "1Re" to "1Ki", "1 Re" to "1Ki", "1 Reyes" to "1Ki",
        "2Re" to "2Ki", "2 Re" to "2Ki", "2 Reyes" to "2Ki",
        "1Cr" to "1Ch", "1 Cr" to "1Ch", "1 Crónicas" to "1Ch",
        "2Cr" to "2Ch", "2 Cr" to "2Ch", "2 Crónicas" to "2Ch",
        "Esd" to "Ezr", "Esdras" to "Ezr",
        "Neh" to "Neh", "Nehemías" to "Neh",
        "Est" to "Est", "Ester" to "Est",
        "Job" to "Job",
        "Sal" to "Psa", "Salmos" to "Psa", "Ps" to "Psa",
        "Pro" to "Pro", "Prov" to "Pro", "Proverbios" to "Pro",
        "Ecl" to "Ecc", "Eclesiastés" to "Ecc",
        "Cant" to "Sng", "Cantares" to "Sng",
        "Isa" to "Isa", "Isaías" to "Isa",
        "Jer" to "Jer", "Jeremías" to "Jer",
        "Lam" to "Lam", "Lamentaciones" to "Lam",
        "Eze" to "Eze", "Ezequiel" to "Eze",
        "Dan" to "Dan", "Daniel" to "Dan",
        "Hos" to "Hos", "Oseas" to "Hos",
        "Joe" to "Joe", "Joel" to "Joe",
        "Amó" to "Amo", "Amo" to "Amo", "Amós" to "Amo",
        "Abd" to "Oba", "Abdías" to "Oba",
        "Jon" to "Jon", "Jonás" to "Jon",
        "Miq" to "Mic", "Miqueas" to "Mic",
        "Nah" to "Nah", "Nahúm" to "Nah",
        "Hab" to "Hab", "Habacuc" to "Hab",
        "Sof" to "Zep", "Sofonías" to "Zep",
        "Hag" to "Hag", "Hageo" to "Hag",
        "Zac" to "Zec", "Zacarías" to "Zec",
        "Mal" to "Mal", "Malaquías" to "Mal",
        "Mat" to "Mat", "Mt" to "Mat", "Mateo" to "Mat",
        "Mar" to "Mar", "Mc" to "Mar", "Marcos" to "Mar",
        "Luc" to "Luk", "Lc" to "Luk", "Lucas" to "Luk",
        "Jua" to "Jhn", "Jn" to "Jhn", "Juan" to "Jhn",
        "Hech" to "Act", "Hch" to "Act", "Hechos" to "Act",
        "Rom" to "Rom", "Ro" to "Rom", "Romanos" to "Rom",
        "1Co" to "1Co", "1 Co" to "1Co", "1 Corintios" to "1Co",
        "2Co" to "2Co", "2 Co" to "2Co", "2 Corintios" to "2Co",
        "Gál" to "Gal", "Gal" to "Gal", "Gálatas" to "Gal",
        "Efe" to "Eph", "Efesios" to "Eph",
        "Fil" to "Php", "Filipenses" to "Php",
        "Col" to "Col", "Colosenses" to "Col",
        "1Te" to "1Th", "1 Ts" to "1Th", "1 Tesalonicenses" to "1Th",
        "2Te" to "2Th", "2 Ts" to "2Th", "2 Tesalonicenses" to "2Th",
        "1Tim" to "1Ti", "1 Ti" to "1Ti", "1 Timoteo" to "1Ti",
        "2Tim" to "2Ti", "2 Ti" to "2Ti", "2 Timoteo" to "2Ti",
        "Tit" to "Tit", "Tito" to "Tit",
        "Flm" to "Phm", "Filemón" to "Phm",
        "Heb" to "Heb", "Hebreos" to "Heb",
        "Sant" to "Jas", "Stgo" to "Jas", "Santiago" to "Jas",
        "1Pe" to "1Pe", "1 Pe" to "1Pe", "1 Pedro" to "1Pe",
        "2Pe" to "2Pe", "2 Pe" to "2Pe", "2 Pedro" to "2Pe",
        "1Jn" to "1Jn", "1 Jn" to "1Jn", "1 Juan" to "1Jn",
        "2Jn" to "2Jn", "2 Jn" to "2Jn", "2 Juan" to "2Jn",
        "3Jn" to "3Jn", "3 Jn" to "3Jn", "3 Juan" to "3Jn",
        "Jud" to "Jud", "Judas" to "Jud",
        "Apoc" to "Rev", "Ap" to "Rev", "Apocalipsis" to "Rev"
    )

    // Regex explanation:
    // 1. (?:[123]\s?)? - Optional leading number (1, 2, 3) followed by optional space
    // 2. [A-ZÁÉÍÓÚ][a-zà-ÿ]{1,11}\.? - Book name or abbreviation (starts with cap, followed by lowercase/unicode, optional dot)
    // 3. \s? - Optional space
    // 4. \d{1,3} - Chapter number (1-3 digits)
    // 5. [:.] - Separator (colon or dot)
    // 6. \d{1,3} - Verse number (1-3 digits)
    private val citationRegex = Regex("""(?:[123]\s?)?[A-ZÁÉÍÓÚ][a-zà-ÿ]{1,11}\.?\s?\d{1,3}[:.]\d{1,3}""")

    fun findCitations(text: String): List<ParsedCitation> {
        return citationRegex.findAll(text).mapNotNull { match ->
            val rawMatch = match.value
            try {
                // Split components. Handling cases like "1 Cor 13:1"
                // First, find where the numbers start
                val firstDigitIndex = rawMatch.indexOfFirst { it.isDigit() }
                if (firstDigitIndex == -1) return@mapNotNull null

                val bookPart = rawMatch.substring(0, firstDigitIndex).trim()
                val numbersPart = rawMatch.substring(firstDigitIndex)

                val (chapter, verse) = if (numbersPart.contains(":")) {
                    numbersPart.split(":").map { it.trim().toInt() }
                } else if (numbersPart.contains(".")) {
                    numbersPart.split(".").map { it.trim().toInt() }
                } else {
                    listOf(numbersPart.trim().toInt(), 1)
                }

                // Map book part to internal ID
                val cleanBookPart = bookPart.removeSuffix(".")
                val bookId = bookNameMap[cleanBookPart] ?: bibleBooks.find { it.name.startsWith(cleanBookPart, ignoreCase = true) }?.id
                
                if (bookId != null) {
                    val bookName = bibleBooks.find { it.id == bookId }?.name ?: bookId
                    ParsedCitation(bookId, bookName, chapter, verse, rawMatch)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.toList()
    }
}
