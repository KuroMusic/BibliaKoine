package com.biblia.koine.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biblia.koine.data.room.BibliaDatabase
import com.biblia.koine.utils.NotificationHelper
import com.biblia.koine.data.BibleBooksMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyVerseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = BibliaDatabase.getDatabase(applicationContext)
            val dao = database.bibleDao()
            
            // Get random verse from database
            val randomVerse = dao.getRandomVerse()
            
            if (randomVerse != null) {
                val bookId = BibleBooksMetadata.getId(randomVerse.book_num) ?: "Gen"
                val bookName = BibleBooksMetadata.getName(bookId)
                val reference = "$bookName ${randomVerse.chapter}:${randomVerse.verse}"
                
                // Show notification
                NotificationHelper.showVerseNotification(
                    context = applicationContext,
                    verseText = randomVerse.text,
                    verseReference = reference
                )
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            android.util.Log.e("DailyVerseWorker", "Error fetching verse", e)
            Result.failure()
        }
    }
}
