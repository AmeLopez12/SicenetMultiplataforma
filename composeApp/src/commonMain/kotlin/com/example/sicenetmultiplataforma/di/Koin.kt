package com.example.sicenetmultiplataforma.di

import androidx.room.RoomDatabase
import com.example.sicenetmultiplataforma.data.local.AppDatabase
import com.example.sicenetmultiplataforma.data.network.SicenetApiService
import com.example.sicenetmultiplataforma.data.repository.ISicenetRepository
import com.example.sicenetmultiplataforma.data.repository.SicenetRepository
import com.example.sicenetmultiplataforma.ui.SicenetViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.compose.viewmodel.dsl.viewModel

// Punto de entrada global para inicializar el motor de inyección de dependencias Koin.
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

// Módulo centralizado que contiene las definiciones de los componentes compartidos.
val commonModule = module {

    // Resuelve dinámicamente el constructor de Room de acuerdo al entorno de ejecución actual.
    single<AppDatabase> {
        // Intenta recuperar el Builder nativo que Android registra usando su Contexto.
        val androidBuilder = getOrNull<androidx.room.RoomDatabase.Builder<AppDatabase>>()

        if (androidBuilder != null) {
            // Entorno Móvil: Construye la base de datos en la ruta protegida del teléfono.
            androidBuilder.build()
        } else {
            // Entorno de Escritorio: Llama de forma directa a la función compilada en jvmMain.
            com.example.sicenetmultiplataforma.data.local.getDatabaseBuilder().build()
        }
    }

    // Registro individual de los DAOs para permitir su inyección directa en el repositorio.
    single { get<AppDatabase>().alumnoDao() }
    single { get<AppDatabase>().materiaDao() }
    single { get<AppDatabase>().kardexDao() }
    single { get<AppDatabase>().califUnidadDao() }
    single { get<AppDatabase>().califFinalDao() }

    // Configuración del cliente HTTP de Ktor optimizado para el Web Service de la escuela.
    single {
        HttpClient {
            // Habilita el almacenamiento automático de cookies para retener la sesión activa.
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }
            // Inyecta cabeceras de red por defecto en todas las peticiones salientes.
            install(DefaultRequest) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                header("Accept", "text/xml, application/soap+xml, */*")
                header("Content-Type", "text/xml; charset=utf-8")

                // Forzar la cabecera Host evita que el servidor IIS del Tec rechace las peticiones con parámetros.
                header("Host", "sicenet.surguanajuato.tecnm.mx")
            }
            // Registra el tráfico de red en la consola interna para facilitar la depuración.
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    // Inyección de dependencias estructurada en capas: Servicio de red -> Repositorio -> ViewModel.
    single { SicenetApiService(get()) }
    single<ISicenetRepository> { SicenetRepository(get(), get(), get(), get(), get(), get()) }
    viewModel { SicenetViewModel(get()) }
}