package com.example.sicenetmultiplataforma.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

@Serializable
@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String = "",
    val nombre: String = "",
    val grupo: String = "",
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

@Serializable
internal data class MateriaRaw(
    val clvOficial: String = "",
    @SerialName("Materia") val nombre: String = "",
    val Grupo: String = "",
    val Docente: String = "",
    val CreditosMateria: Int = 0,
    val Lunes: String = "",
    val Martes: String = "",
    val Miercoles: String = "",
    val Jueves: String = "",
    val Viernes: String = "",
    val Sabado: String = "",
    val Domingo: String = ""
)