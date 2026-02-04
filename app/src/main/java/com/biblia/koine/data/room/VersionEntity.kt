package com.biblia.koine.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "versiones")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null, // Se usa nullable porque en el SQL 'id' no tiene NOT NULL

    @ColumnInfo(name = "codigo")
    val codigo: String,

    @ColumnInfo(name = "nombre_completo") // Mapea 'nombre' del error a 'nombre_completo' del SQL
    val nombre: String,

    @ColumnInfo(name = "abreviatura")     // Mapea 'copyright' del error a 'abreviatura' del SQL
    val abreviatura: String
)
