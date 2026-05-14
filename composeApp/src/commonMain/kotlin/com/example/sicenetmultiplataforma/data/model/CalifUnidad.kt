package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
@Entity(tableName = "calif_unidades")
data class CalifUnidad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val materia: String = "",
    val unidades: String = "", // Guardado como String JSON para Room
    val lastUpdate: Long = 0L
) {
    @Serializable
    internal data class CalifUnidadRaw(
        @SerialName("Materia") val materia: String = ""
        // Aquí puedes agregar campos fijos si los conoces (ej. C1, C2)
    )

    companion object {
        private val jsonConfig = Json {
            ignoreUnknownKeys = true
        }

        fun fromJsonList(jsonString: String): List<CalifUnidad> {
            return try {
                val rawList = jsonConfig.decodeFromString<List<kotlinx.serialization.json.JsonObject>>(jsonString)

                rawList.map { jsonObject ->
                    CalifUnidad(
                        materia = jsonObject["Materia"]?.toString()?.replace("\"", "") ?: "Sin Nombre",
                        unidades = jsonObject.toString(), // Guardamos el JSON completo para la UI
                        lastUpdate = 0L
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}