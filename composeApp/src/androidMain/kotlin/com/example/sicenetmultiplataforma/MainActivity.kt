package com.example.sicenetmultiplataforma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.RoomDatabase
import com.example.sicenetmultiplataforma.data.local.AppDatabase
import com.example.sicenetmultiplataforma.data.local.getAndroidDatabaseBuilder
import com.example.sicenetmultiplataforma.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Detenemos cualquier instancia previa de Koin para evitar colisiones en caliente
        stopKoin()

        // Inicializamos Koin inyectando el contexto real de Android
        startKoin {
            androidContext(this@MainActivity)
            modules(commonModule, module {
                // SOLUCIÓN: Proveemos explícitamente el Builder nativo que espera el módulo común
                single<RoomDatabase.Builder<AppDatabase>> {
                    getAndroidDatabaseBuilder(get())
                }
            })
        }

        setContent {
            App() // Arranca tu enrutador unificado de App.kt
        }
    }
}