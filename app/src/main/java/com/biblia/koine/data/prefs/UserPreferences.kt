package com.biblia.koine.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

val Context.userDataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    private val dataStore = context.userDataStore

    object Keys {
        // Apariencia
        val THEME = stringPreferencesKey("theme")                 // "claro", "oscuro", "auto"
        val FONT_SIZE = intPreferencesKey("font_size")            // 14..26
        val FONT_FAMILY = stringPreferencesKey("font_family")     // "System", "Serif", etc.
        val LINE_SPACING = floatPreferencesKey("line_spacing")    // 1.0f..2.0f

        // Texto y lectura
        val BIBLE_VERSION = stringPreferencesKey("bible_version") // "RV1960"
        val SHOW_RED_LETTERS = booleanPreferencesKey("red_letters")
        val SHOW_VERSE_NUMBERS = booleanPreferencesKey("verse_numbers")
        val SHOW_SECTION_TITLES = booleanPreferencesKey("section_titles")
        val SHOW_CROSS_REFERENCES = booleanPreferencesKey("cross_refs")
        val JUSTIFY_TEXT = booleanPreferencesKey("justify_text")
        val CONTINUOUS_READING = booleanPreferencesKey("continuous_reading")
        val ENABLE_STRONGS = booleanPreferencesKey("enable_strongs") // NEW: Show Strong's numbers

        // Navegación
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SWIPE_CHAPTERS = booleanPreferencesKey("swipe_chapters")
        val ANIMATIONS = booleanPreferencesKey("animations")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")

        // Notificaciones
        val DAILY_VERSE = booleanPreferencesKey("daily_verse")
        val READING_REMINDER = booleanPreferencesKey("reading_reminder")
        val REMINDER_TIME = stringPreferencesKey("reminder_time") // "09:00"
        
        // Verse of Day (NEW)
        val VERSE_OF_DAY_DATE = longPreferencesKey("verse_of_day_date") // Day epoch to detect changes
        val VERSE_OF_DAY_REF = stringPreferencesKey("verse_of_day_ref")  // "John 3:16"
        
        // Persistence
        val LAST_BOOK = stringPreferencesKey("last_book")
        val LAST_CHAPTER = intPreferencesKey("last_chapter")
        val LAST_VERSE = intPreferencesKey("last_verse")
    }

    data class Prefs(
        // Apariencia
        val theme: String = "auto",
        val fontSize: Int = 18,
        val fontFamily: String = "System",
        val lineSpacing: Float = 1.4f,

        // Texto y lectura
        val bibleVersion: String = "RV1960",
        val showRedLetters: Boolean = true,
        val showVerseNumbers: Boolean = true,
        val showSectionTitles: Boolean = true,
        val showCrossReferences: Boolean = false,
        val justifyText: Boolean = false,
        val continuousReading: Boolean = false,
        val enableStrongs: Boolean = false, // NEW

        // Navegación
        val keepScreenOn: Boolean = true,
        val swipeChapters: Boolean = true,
        val animations: Boolean = true,
        val hapticFeedback: Boolean = false,

        // Notificaciones
        val dailyVerse: Boolean = true,
        val readingReminder: Boolean = false,
        val reminderTime: String = "09:00",
        
        // Verse of Day (NEW)
        val verseOfDayDate: Long = 0L,
        val verseOfDayRef: String = "",
        
        // Persistence
        val lastBookId: String = "",
        val lastChapter: Int = 1,
        val lastVerse: Int = 1
    )

    val prefsFlow: Flow<Prefs> = dataStore.data.map { pref ->
        Prefs(
            theme = pref[Keys.THEME] ?: "auto",
            fontSize = pref[Keys.FONT_SIZE] ?: 18,
            fontFamily = pref[Keys.FONT_FAMILY] ?: "System",
            lineSpacing = pref[Keys.LINE_SPACING] ?: 1.4f,
            bibleVersion = pref[Keys.BIBLE_VERSION] ?: "RV1960",
            showRedLetters = pref[Keys.SHOW_RED_LETTERS] ?: true,
            showVerseNumbers = pref[Keys.SHOW_VERSE_NUMBERS] ?: true,
            showSectionTitles = pref[Keys.SHOW_SECTION_TITLES] ?: true,
            showCrossReferences = pref[Keys.SHOW_CROSS_REFERENCES] ?: false,
            justifyText = pref[Keys.JUSTIFY_TEXT] ?: false,
            continuousReading = pref[Keys.CONTINUOUS_READING] ?: false,
            enableStrongs = pref[Keys.ENABLE_STRONGS] ?: false,
            keepScreenOn = pref[Keys.KEEP_SCREEN_ON] ?: true,
            swipeChapters = pref[Keys.SWIPE_CHAPTERS] ?: true,
            animations = pref[Keys.ANIMATIONS] ?: true,
            hapticFeedback = pref[Keys.HAPTIC_FEEDBACK] ?: false,
            dailyVerse = pref[Keys.DAILY_VERSE] ?: true,
            readingReminder = pref[Keys.READING_REMINDER] ?: false,
            reminderTime = pref[Keys.REMINDER_TIME] ?: "09:00",
            verseOfDayDate = pref[Keys.VERSE_OF_DAY_DATE] ?: 0L,
            verseOfDayRef = pref[Keys.VERSE_OF_DAY_REF] ?: "",
            lastBookId = pref[Keys.LAST_BOOK] ?: "MAT", // Default to Matthew if nothing saved
            lastChapter = pref[Keys.LAST_CHAPTER] ?: 1,
            lastVerse = pref[Keys.LAST_VERSE] ?: 1
        )
    }

    // Funciones de guardado

    suspend fun setTheme(value: String) = dataStore.edit { it[Keys.THEME] = value }
    suspend fun setFontSize(value: Int) = dataStore.edit { it[Keys.FONT_SIZE] = value }
    suspend fun setFontFamily(value: String) = dataStore.edit { it[Keys.FONT_FAMILY] = value }
    suspend fun setLineSpacing(value: Float) = dataStore.edit { it[Keys.LINE_SPACING] = value }

    suspend fun setBibleVersion(value: String) = dataStore.edit { it[Keys.BIBLE_VERSION] = value }
    suspend fun setShowRedLetters(value: Boolean) = dataStore.edit { it[Keys.SHOW_RED_LETTERS] = value }
    suspend fun setShowVerseNumbers(value: Boolean) = dataStore.edit { it[Keys.SHOW_VERSE_NUMBERS] = value }
    suspend fun setShowSectionTitles(value: Boolean) = dataStore.edit { it[Keys.SHOW_SECTION_TITLES] = value }
    suspend fun setShowCrossReferences(value: Boolean) = dataStore.edit { it[Keys.SHOW_CROSS_REFERENCES] = value }
    suspend fun setJustifyText(value: Boolean) = dataStore.edit { it[Keys.JUSTIFY_TEXT] = value }
    suspend fun setContinuousReading(value: Boolean) = dataStore.edit { it[Keys.CONTINUOUS_READING] = value }

    suspend fun setKeepScreenOn(value: Boolean) = dataStore.edit { it[Keys.KEEP_SCREEN_ON] = value }
    suspend fun setSwipeChapters(value: Boolean) = dataStore.edit { it[Keys.SWIPE_CHAPTERS] = value }
    suspend fun setAnimations(value: Boolean) = dataStore.edit { it[Keys.ANIMATIONS] = value }
    suspend fun setHapticFeedback(value: Boolean) = dataStore.edit { it[Keys.HAPTIC_FEEDBACK] = value }

    suspend fun setDailyVerse(value: Boolean) = dataStore.edit { it[Keys.DAILY_VERSE] = value }
    suspend fun setReadingReminder(value: Boolean) = dataStore.edit { it[Keys.READING_REMINDER] = value }
    suspend fun setReminderTime(value: String) = dataStore.edit { it[Keys.REMINDER_TIME] = value }
    
    suspend fun setEnableStrongs(value: Boolean) = dataStore.edit { it[Keys.ENABLE_STRONGS] = value }
    suspend fun setVerseOfDayDate(value: Long) = dataStore.edit { it[Keys.VERSE_OF_DAY_DATE] = value }
    suspend fun setVerseOfDayRef(value: String) = dataStore.edit { it[Keys.VERSE_OF_DAY_REF] = value }
    
    // Reading Persistence
    suspend fun setLastBook(value: String) = dataStore.edit { it[Keys.LAST_BOOK] = value }
    suspend fun setLastChapter(value: Int) = dataStore.edit { it[Keys.LAST_CHAPTER] = value }
    suspend fun setLastVerse(value: Int) = dataStore.edit { it[Keys.LAST_VERSE] = value }

    suspend fun setLastPosition(bookId: String, chapter: Int, verse: Int) {
        dataStore.edit { pref ->
            pref[Keys.LAST_BOOK] = bookId
            pref[Keys.LAST_CHAPTER] = chapter
            pref[Keys.LAST_VERSE] = verse
        }
    }

    // Helper methods for simplified non-flow access (Coroutine-friendly)
    suspend fun getLastVersion(): String? {
        // Read directly from DataStore
        val prefs = dataStore.data.firstOrNull()
        return prefs?.get(Keys.BIBLE_VERSION)
    }

    suspend fun saveLastVersion(version: String) {
        setBibleVersion(version)
    }
}
