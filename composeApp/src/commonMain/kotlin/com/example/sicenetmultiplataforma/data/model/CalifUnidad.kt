package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.*

// Tabla local para las calificaciones parciales. Almacena las unidades dinámicas en un String plano.
@Serializable
@Entity(tableName = "calif_unidades")
data class CalifUnidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerialName("Materia") val materia: String = "",
    val unidades: String = "", // Guarda la estructura JSON completa de las notas por unidad
    val lastUpdate: Long = 0L
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

        // Extrae el arreglo de materias del JSON y mapea el objeto de cada una a texto plano.
        fun fromJsonList(jsonString: String): List<CalifUnidad> {
            if (jsonString.isBlank()) return emptyList()
            return try {
                val element = json.parseToJsonElement(jsonString)
                val rawList = when (element) {
                    is JsonArray -> element
                    is JsonObject -> {
                        val firstArray = element.values.filterIsInstance<JsonArray>().firstOrNull()
                        if (firstArray != null) firstArray
                        else {
                            val firstString = element.values.filterIsInstance<JsonPrimitive>().firstOrNull { it.isString }
                            if (firstString != null) json.parseToJsonElement(firstString.content) as? JsonArray
                            else null
                        }
                    }
                    else -> null
                }

                rawList?.map {
                    val obj = it.jsonObject
                    CalifUnidad(
                        materia = obj["Materia"]?.jsonPrimitive?.content ?: "",
                        unidades = obj.toString(), // Guarda el mapa interno de calificaciones C1, C2...
                        lastUpdate = 0L
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}