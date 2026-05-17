package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import androidx.room.Ignore
import kotlinx.serialization.SerialName

// Tabla local para los horarios, docentes y grupos de las asignaturas en curso.
@Serializable
@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String = "",
    val nombre: String = "",
    val grupo: String = "", // Mapeado interno para Room
    val docente: String = "",
    val creditos: Int = 0,
    val lunes: String = "",
    val martes: String = "",
    val miercoles: String = "",
    val jueves: String = "",
    val viernes: String = "",
    val sabado: String = "",
    val domingo: String = "",
    val aula: String = "",
    val lastUpdate: Long = 0L
)

// Mapea los nombres de llaves en camelCase del JSON original de la carga de Sicenet.
@Serializable
data class MateriaRaw(
    @SerialName("clvOficial") val clvOficial: String = "",
    @SerialName("materia") val nombre: String = "",
    @SerialName("grupo") val grupo: String = "",
    @SerialName("docente") val docente: String = "",
    @SerialName("creditosMateria") val creditosMateria: Int = 0,
    @SerialName("lunes") val lunes: String = "",
    @SerialName("martes") val martes: String = "",
    @SerialName("miercoles") val miercoles: String = "",
    @SerialName("jueves") val jueves: String = "",
    @SerialName("viernes") val viernes: String = "",
    @SerialName("sabado") val sabado: String = "",
    @SerialName("domingo") val domingo: String = ""
)