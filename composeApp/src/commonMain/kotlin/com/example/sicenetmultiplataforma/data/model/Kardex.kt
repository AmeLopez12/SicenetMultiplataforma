package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.*

// Tabla local para almacenar el historial de materias acreditadas en semestres anteriores.
@Serializable
@Entity(tableName = "kardex")
data class Kardex(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String = "",
    val materia: String = "",
    val calificacion: String = "",
    val periodo: String = "",
    val lastUpdate: Long = 0L
)

// Modelo temporal para parsear las propiedades estructuradas del Kardex de la escuela.
@Serializable
data class KardexRaw(
    @SerialName("Clave") val clave: String = "",
    @SerialName("Materia") val materia: String = "",
    @SerialName("Calificacion") val calificacion: String = "",
    @SerialName("Periodo") val periodo: String = ""
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

        // Transforma la respuesta de red en la lista inmutable que consume la interfaz gráfica.
        fun fromJsonList(jsonString: String): List<Kardex> {
            if (jsonString.isBlank() || jsonString == "[]") return emptyList()
            return try {
                val element = json.parseToJsonElement(jsonString)
                val jsonArray = when (element) {
                    is JsonArray -> element
                    is JsonObject -> element.values.firstOrNull() as? JsonArray
                    else -> null
                }

                jsonArray?.map { item ->
                    val raw = json.decodeFromJsonElement<KardexRaw>(item)
                    Kardex(
                        clave = raw.clave,
                        materia = raw.materia,
                        calificacion = raw.calificacion,
                        periodo = raw.periodo
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}