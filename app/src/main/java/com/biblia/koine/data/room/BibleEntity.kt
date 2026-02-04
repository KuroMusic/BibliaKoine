package com.biblia.koine.data.room

import androidx.room.*

@Entity(
    tableName = "bible_content",
    indices = [
        Index(value = ["book_num", "chapter"], name = "idx_bible_content_book_cap"),
        Index(value = ["version", "book_num", "chapter"], name = "idx_bible_content_lookup"),
        Index(value = ["book_num", "chapter", "version"], name = "index_book_chapter_version")
    ]
)
data class BibleEntity(
    @PrimaryKey val id: Int,
    val version: String,
    val book_num: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
    @ColumnInfo(name = "is_red", defaultValue = "0") val is_red: Int? = 0,
    val strongs: String? = null
)
