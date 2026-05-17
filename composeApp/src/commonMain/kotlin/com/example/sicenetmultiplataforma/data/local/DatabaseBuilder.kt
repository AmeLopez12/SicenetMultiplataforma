package com.example.sicenetmultiplataforma.data.local

import androidx.room.RoomDatabase

// Define el constructor de la base de datos que cada plataforma debe implementar.
// Android requiere el Contexto de la app, mientras que Desktop requiere una ruta de archivos Java.
expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>