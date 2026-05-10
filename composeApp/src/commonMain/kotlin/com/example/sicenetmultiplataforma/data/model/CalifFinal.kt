package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = "calif_final")
data class CalifFinal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val materia: String = "",
    @SerialName("clvMat")
    val clave: String = "",
    @SerialName("calif")
    val calificacion: Int = 0,
    val acreditacion: String = "",
    val lastUpdate: Long = 0L // Inicializado en 0 para compatibilidad KMP
) {
    companion object {
        private val jsonConfig = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun fromJsonList(jsonString: String): List<CalifFinal> {
            return try {
                jsonConfig.decodeFromString<List<CalifFinal>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}