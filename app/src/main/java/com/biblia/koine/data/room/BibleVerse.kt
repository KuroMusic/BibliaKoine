package com.biblia.koine.data.room

import androidx.room.ColumnInfo
import androidx.room.Index

data class BibleVerse(
    val id: Int,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "book_num") val book_num: Int,
    @ColumnInfo(name = "chapter") val chapter: Int,
    @ColumnInfo(name = "verse") val verse: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "is_red") val is_red: Int? = 0,
    @ColumnInfo(name = "strongs") val strongs: String? = null
)
