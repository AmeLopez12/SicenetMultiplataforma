package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = "kardex")
data class Kardex(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String = "",
    val materia: String = "",
    val calificacion: Int = 0,
    val periodo: String = "",
    val acreditacion: String = "",
    val lastUpdate: Long = 0L
) {
    @Serializable
    private data class KardexResponse(val lstKardex: List<KardexRaw>)

    @Serializable
    private data class KardexRaw(
        val ClvOfiMat: String = "",
        val Materia: String = "",
        val Calif: Int = 0,
        val P1: String = "",
        val A1: String = "",
        val Acred: String = ""
    )

    companion object {
        private val jsonConfig = Json { ignoreUnknownKeys = true }

        fun fromJsonList(jsonString: String): List<Kardex> {
            return try {
                val response = jsonConfig.decodeFromString<KardexResponse>(jsonString)
                response.lstKardex.map { raw ->
                    Kardex(
                        clave = raw.ClvOfiMat,
                        materia = raw.Materia,
                        calificacion = raw.Calif,
                        periodo = if (raw.P1.isNotBlank()) "${raw.P1} ${raw.A1}" else "Desconocido",
                        acreditacion = raw.Acred,
                        lastUpdate = 0L // En KMP evita System.currentTimeMillis() en el modelo común
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}