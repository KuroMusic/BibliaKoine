package com.biblia.koine.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strong_concord")
data class StrongDefinition(
    @PrimaryKey @ColumnInfo(name = "topic") val topic: String,
    @ColumnInfo(name = "definition") val definition: String? = "",
    @ColumnInfo(name = "is_strong", defaultValue = "0") val isStrong: Int? = 0,
    @ColumnInfo(name = "is_concord", defaultValue = "0") val isConcord: Int? = 0
)
