package com.example.sicenetmultiplataforma.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sicenetmultiplataforma.data.local.SicenetDatabase

// El cambio es usar 'actual' en lugar de 'fun' normal
actual fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase> {
    // Obtenemos el contexto que guardamos previamente (te explico abajo)
    val context = AppContext.get()
    val dbFile = context.getDatabasePath("sicenet_database.db")

    return Room.databaseBuilder<SicenetDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}
object AppContext {
    private lateinit var instance: Context
    fun set(context: Context) { instance = context }
    fun get(): Context = instance
}