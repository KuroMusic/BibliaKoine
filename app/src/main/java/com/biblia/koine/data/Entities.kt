package com.biblia.koine.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

// Existing entities
@Entity(tableName = "verses")
data class Verse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int, // e.g. 43
    val chapter: Int,
    val verse: Int,
    val text: String,
    val version: String = "RV1960",
    // New fields optional for room compatibility, but we might just use separate tables for user data
    // keeping Verse as "Static Content" roughly
)

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: Int,
    val name: String,
    val testament: String
)

// NEW ENTITIES

@Entity(
    tableName = "highlights",
    indices = [
        Index(value = ["bookId", "chapter", "verse"], unique = true),
        Index(value = ["createdAt"])
    ]
)
data class Highlight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String, // "Jhn" or "43" - sticking to String ID from Sword to make it easier
    val chapter: Int,
    val verse: Int,
    val color: String,  // "#FFEB3B", "#FF5722", etc
    val version: String = "default",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val verseText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey val id: Int = 1,
    val currentBookId: String = "John",
    val currentChapter: Int = 1,
    val lastReadVerse: Int = 1,
    val lastScrollOffset: Float = 0f, // NEW: Scroll position persistence
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val daysStreak: Int = 0,
    val chaptersRead: Int = 0,
    val highlightsCount: Int = 0,
    val notesCount: Int = 0,
    val lastReadDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
