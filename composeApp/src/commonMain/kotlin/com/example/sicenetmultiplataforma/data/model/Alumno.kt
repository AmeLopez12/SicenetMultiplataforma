package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = "alumno_perfil")
data class Alumno(
    @PrimaryKey
    val matricula: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val especialidad: String = "",
    val semActual: Int = 0,
    val cdtosAcumulados: Int = 0,
    val cdtosActuales: Int = 0,
    val estatus: String = "",
    val inscrito: Boolean = false,
    val fechaReins: String = "",
    val modEducativo: Int = 0,
    val adeudo: Boolean = false,
    val adeudoDescripcion: String = "",
    val urlFoto: String = "",
    val lineamiento: Int = 0,
    val promedioGeneral: String = "N/A",
    val lastUpdate: Long = 0L
) {
    companion object {
        private val jsonConfig = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        fun fromJson(jsonString: String): Alumno {
            return try {
                jsonConfig.decodeFromString<Alumno>(jsonString)
            } catch (e: Exception) {
                // Si hay un error en el parseo, devuelve un objeto vacío
                Alumno()
            }
        }
    }
}
