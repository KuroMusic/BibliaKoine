package com.biblia.koine.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biblia.koine.data.BibleDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class StreakReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = BibleDatabase.getDatabase(applicationContext)
        val stats = database.userStatsDao().getStats().firstOrNull()
        
        val lastReadDate = stats?.lastReadDate ?: 0L
        
        // Midnight of today
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        // If last read was before today or never read
        if (lastReadDate < todayStart) {
            showNotification()
        }
        
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "streak_reminder"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Recordatorio de Lectura", 
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_edit) // Basic icon
            .setContentTitle("¡No pierdas tu racha!")
            .setContentText("Lee el Evangelio de hoy en KuroStream")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(777, notification)
    }
}
