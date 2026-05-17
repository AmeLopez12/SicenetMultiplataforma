package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.*

// Define el esquema de la tabla local y habilita la serialización JSON.
@Serializable
@Entity(tableName = "alumno_perfil")
data class Alumno(
    @PrimaryKey
    @SerialName("matricula") val matricula: String = "",
    @SerialName("nombre") val nombre: String = "",
    @SerialName("carrera") val carrera: String = "",
    @SerialName("especialidad") val especialidad: String = "",
    @SerialName("semActual") val semActual: Int = 0,
    @SerialName("cdtosAcumulados") val cdtosAcumulados: Int = 0,
    @SerialName("cdtosActuales") val cdtosActuales: Int = 0,
    @SerialName("estatus") val estatus: String = "",
    @SerialName("inscrito") val inscrito: Boolean = false,
    @SerialName("fechaReins") val fechaReins: String = "",
    @SerialName("modEducativo") val modEducativo: Int = 0,
    @SerialName("adeudo") val adeudo: Boolean = false,
    @SerialName("adeudoDescripcion") val adeudoDescripcion: String = "",
    @SerialName("urlFoto") val urlFoto: String = "",
    @SerialName("lineamiento") val lineamiento: Int = 0,
    @SerialName("promedioGeneral") val promedioGeneral: String = "N/A",
    // Almacena la marca de tiempo de la última sincronización con el servidor.
    val lastUpdate: Long = 0L
) {
    companion object {
        // Configuración tolerante del parser JSON para ignorar llaves desconocidas o nulas.
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        // Convierte el String JSON del servidor en un objeto Alumno.
        // Soporta formatos anidados como la envoltura "d" o arreglos indirectos de .NET.
        fun fromJson(jsonString: String): Alumno {
            if (jsonString.isBlank()) return Alumno()
            return try {
                val element = json.parseToJsonElement(jsonString)
                val targetObject = when (element) {
                    is JsonObject -> {
                        val d = element["d"]
                        // Extrae el contenido si viene dentro de la propiedad "d" de .NET.
                        if (d is JsonPrimitive && d.isString) {
                            val inner = json.parseToJsonElement(d.content)
                            if (inner is JsonArray && inner.isNotEmpty()) inner[0].jsonObject
                            else if (inner is JsonObject) inner
                            else null
                        } else element
                    }
                    // Maneja la respuesta si viene directamente como un arreglo.
                    is JsonArray -> if (element.isNotEmpty()) element[0].jsonObject else null
                    else -> null
                }
                // Decodifica el objeto JSON final al modelo Alumno.
                if (targetObject != null) json.decodeFromJsonElement<Alumno>(targetObject) else Alumno()
            } catch (e: Exception) {
                Alumno()
            }
        }
    }
}