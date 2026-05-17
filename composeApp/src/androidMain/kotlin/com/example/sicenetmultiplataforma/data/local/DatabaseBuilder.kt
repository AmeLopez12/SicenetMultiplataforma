package com.example.sicenetmultiplataforma.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// Lanza un error intencional si se intenta inicializar la base de datos sin el contexto de Android.
// Esto protege al grafo de dependencias de inicializaciones vacías o erróneas.
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    throw NotImplementedError("Usa la inyección por contexto mediante Koin en el inicio de Android")
}

// Inicializador oficial para la plataforma móvil. Recibe el contexto del ciclo de vida de Android.
fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    // Determina la ruta interna y segura dentro de la carpeta 'databases' exclusiva de la app.
    val dbFile = context.getDatabasePath("sicenet_multiplataforma.db")

    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    ).fallbackToDestructiveMigration(dropAllTables = true) // Reconstruye las tablas automáticamente ante cambios de esquema.
}