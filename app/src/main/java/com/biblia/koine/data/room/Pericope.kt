package com.biblia.koine.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "pericopas",
    indices = [
        Index(value = ["libro_id", "capitulo_inicio"], name = "idx_pericopas_coords"),
        Index(value = ["version_id"], name = "idx_pericopas_version"),
        Index(value = ["libro_id", "capitulo_inicio", "versiculo_inicio"], name = "idx_pericopas_libro")
    ]
)
data class Pericope(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int, 
    @ColumnInfo(name = "version_id") val versionId: Int,
    @ColumnInfo(name = "libro_id") val bookId: Int,
    @ColumnInfo(name = "titulo") val title: String,
    @ColumnInfo(name = "capitulo_inicio") val startChapter: Int,
    @ColumnInfo(name = "versiculo_inicio") val startVerse: Int,
    @ColumnInfo(name = "capitulo_fin") val endChapter: Int,
    @ColumnInfo(name = "versiculo_fin") val endVerse: Int
)
