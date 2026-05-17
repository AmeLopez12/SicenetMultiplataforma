package com.example.sicenetmultiplataforma.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // Localiza la raíz del directorio de usuario de Windows/Mac/Linux para guardar el archivo físico.
    val dbFile = File(System.getProperty("user.home"), "sicenet_database.db")

    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
        // Evita el colapso por inconsistencias de hash eliminando la BD vieja si cambias el modelo de datos.
        .fallbackToDestructiveMigration(true)

        // Optimiza el rendimiento en Desktop forzando a SQLite a plasmar las listas en el disco de inmediato.
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)

        // Asigna el controlador multiplataforma embebido para interactuar con el motor SQLite nativo.
        .setDriver(BundledSQLiteDriver())
}