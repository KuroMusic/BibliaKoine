package com.biblia.koine.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DatabaseModule {
    private val _isDatabaseReady = MutableStateFlow(false)
    val isDatabaseReady: StateFlow<Boolean> = _isDatabaseReady.asStateFlow()

    fun setDatabaseReady(ready: Boolean) {
        _isDatabaseReady.value = ready
    }
}
