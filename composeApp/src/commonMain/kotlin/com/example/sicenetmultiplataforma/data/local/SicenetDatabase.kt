package com.example.sicenetmultiplataforma.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.example.sicenet.data.local.CalifFinalDao
import com.example.sicenet.data.local.CalifUnidadDao
import com.example.sicenet.data.local.KardexDao
import com.example.sicenet.data.local.MateriaDao
import com.example.sicenetmultiplataforma.data.model.*

@Database(
    entities = [Alumno::class, Materia::class, Kardex::class, CalifUnidad::class, CalifFinal::class],
    version = 4,
    exportSchema = false
)
@ConstructedBy(SicenetDatabaseConstructor::class)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    abstract fun materiaDao(): MateriaDao
    abstract fun kardexDao(): KardexDao
    abstract fun califUnidadDao(): CalifUnidadDao
    abstract fun califFinalDao(): CalifFinalDao
}

// Esto le dice a Room que genere el código de implementación en KMP
expect object SicenetDatabaseConstructor : RoomDatabaseConstructor<SicenetDatabase>

/**
 * Función para obtener el builder en cada plataforma.
 * Se implementará en androidMain, iosMain, etc.
 */
expect fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase>