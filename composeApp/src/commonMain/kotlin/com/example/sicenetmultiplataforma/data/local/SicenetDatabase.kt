package com.example.sicenetmultiplataforma.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.sicenetmultiplataforma.data.model.*

@Database(
    entities = [
        Alumno::class,
        Materia::class,
        Kardex::class,
        CalifUnidad::class,
        CalifFinal::class
    ],
    version = 2
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    abstract fun materiaDao(): MateriaDao
    abstract fun kardexDao(): KardexDao
    abstract fun califUnidadDao(): CalifUnidadDao
    abstract fun califFinalDao(): CalifFinalDao
}