package com.biblia.koine.utils

import com.biblia.koine.data.BibleBooksMetadata

fun getBookNameSpanish(bookId: String): String {
    // Single source of truth is BibleBooksMetadata
    return BibleBooksMetadata.getName(bookId)
}
