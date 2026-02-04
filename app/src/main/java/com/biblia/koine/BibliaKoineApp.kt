package com.biblia.koine

import android.app.Application
import com.biblia.koine.data.cache.BibleCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.biblia.koine.worker.StreakReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * BibliaKoineApp - Application class para inicialización global
 * 
 * Optimización YouVersion: Carga datos estáticos en memoria al iniciar
 * para que el selector de libros y otras pantallas sean instantáneas.
 */
class BibliaKoineApp : Application() {
    
    // Application scope for background initialization
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar cache de libros en background
        applicationScope.launch {
            BibleCache.initialize(this@BibliaKoineApp)
        }

        // Programar recordatorio de racha (8:00 PM)
        scheduleStreakReminder()
    }

    private fun scheduleStreakReminder() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Target 8:00 PM
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val delay = calendar.timeInMillis - now
        
        val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "streak_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
