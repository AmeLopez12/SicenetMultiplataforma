package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.*

// Tabla local de Room para las calificaciones definitivas del semestre.
@Serializable
@Entity(tableName = "calificaciones_finales")
data class CalifFinal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String = "",
    val calificacion: Int = 0,
    val acreditacion: String = "",
    val lastUpdate: Long = 0L
)

// Modelo intermedio para procesar las llaves en PascalCase que envía el servidor.
@Serializable
data class CalifFinalRaw(
    @SerialName("Materia") val materia: String = "",
    @SerialName("Calificacion") val calificacion: Int = 0,
    @SerialName("Acreditacion") val acreditacion: String = ""
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

        // Convierte el JSON crudo en una lista limpia de entidades para la base de datos.
        fun fromJsonList(jsonString: String): List<CalifFinal> {
            if (jsonString.isBlank()) return emptyList()
            return try {
                val element = json.parseToJsonElement(jsonString)
                val jsonArray = when (element) {
                    is JsonArray -> element
                    is JsonObject -> element.values.firstOrNull() as? JsonArray
                    else -> null
                }

                jsonArray?.map { item ->
                    val raw = json.decodeFromJsonElement<CalifFinalRaw>(item)
                    CalifFinal(
                        materia = raw.materia,
                        calificacion = raw.calificacion,
                        acreditacion = raw.acreditacion
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}