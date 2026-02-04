package com.biblia.koine.data

object BibleBooksMetadata {
    
    // Derived maps for performance
    private val bookData = bibleBooks.map { Triple(it.id, it.name, it.chapters) }
    private val books = bibleBooks.map { Triple(it.bookNumber, it.id, it.name) }

    fun getNumber(id: String): Int {
        return books.find { it.second.equals(id, ignoreCase = true) }?.first ?: 1
    }

    fun getId(number: Int): String? {
        return books.find { it.first == number }?.second
    }

    fun getName(id: String): String {
        return books.find { it.second.equals(id, ignoreCase = true) }?.third ?: id
    }
    
    // Page Turn Logic
    
    fun getTotalChapters(): Int {
        return bookData.sumOf { it.third }
    }
    
    // Convert 0..1188 index to (BookId, Chapter)
    fun getChapterFromIndex(index: Int): Pair<String, Int> {
        var remaining = index
        for (book in bookData) {
            if (remaining < book.third) {
                return Pair(book.first, remaining + 1)
            }
            remaining -= book.third
        }
        return Pair("Gen", 1) // Fallback
    }
    
    // Convert (BookId, Chapter) to 0..1188 index
    fun getIndexFromChapter(bookId: String, chapter: Int): Int {
        var index = 0
        for (book in bookData) {
            if (book.first.equals(bookId, ignoreCase = true)) {
                return index + (chapter - 1).coerceAtLeast(0)
            }
            index += book.third
        }
        return 0 // Fallback
    }
    
    fun getNextChapter(bookId: String, chapter: Int): Pair<String, Int>? {
        val index = getIndexFromChapter(bookId, chapter)
        if (index + 1 < getTotalChapters()) {
            return getChapterFromIndex(index + 1)
        }
        return null
    }

    fun getPrevChapter(bookId: String, chapter: Int): Pair<String, Int>? {
        val index = getIndexFromChapter(bookId, chapter)
        if (index - 1 >= 0) {
            return getChapterFromIndex(index - 1)
        }
        return null
    }
}
