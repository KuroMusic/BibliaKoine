package com.biblia.koine.utils

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Global crash handler and error utilities for BibliaKoine
 * Prevents crashes from propagating and provides safe default values
 */
object CrashHandler {
    const val TAG = "BibliaKoine"
    
    /**
     * Coroutine exception handler - catches unhandled exceptions in ViewModels/Repositories
     */
    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Unhandled coroutine exception: ${throwable.message}", throwable)
        throwable.printStackTrace()
        // In production, you could send this to Firebase Crashlytics:
        // FirebaseCrashlytics.getInstance().recordException(throwable)
    }
    
    /**
     * Safe try-catch wrapper for database/network operations
     * Returns result or default value on exception
     */
    inline fun <T> safeTry(
        defaultValue: T,
        logTag: String = TAG,
        operation: () -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(logTag, "Operation failed: ${e.message}", e)
            defaultValue
        }
    }
    
    /**
     * Safe async operation  - doesn't throw
     */
    suspend inline fun <T> safeAsync(
        defaultValue: T,
        logTag: String = TAG,
        crossinline operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            Log.e(logTag, "Async operation failed: ${e.message}", e)
            defaultValue
        }
    }
    
    /**
     * Log non-fatal errors without crashing
     */
    fun logError(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
        // FirebaseCrashlytics.getInstance().log("$tag: $message")
        throwable?.let {
            // FirebaseCrashlytics.getInstance().recordException(it)
        }
    }
}
