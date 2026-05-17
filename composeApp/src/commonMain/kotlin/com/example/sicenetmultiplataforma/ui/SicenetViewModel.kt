package com.example.sicenetmultiplataforma.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicenetmultiplataforma.data.repository.ISicenetRepository
import com.example.sicenetmultiplataforma.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SicenetViewModel(private val repository: ISicenetRepository) : ViewModel() {

    // Representa el estado actual de la pantalla de autenticación.
    sealed class LoginResult {
        object Idle : LoginResult()
        object Loading : LoginResult()
        object Success : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    var loginState by mutableStateOf<LoginResult>(LoginResult.Idle)
        private set

    // Estados en memoria para pintar la UI de inmediato sin esperar los hilos de Room.
    var alumnoDirecto by mutableStateOf<Alumno?>(null)
        private set
    var cargaDirecta by mutableStateOf<List<Materia>>(emptyList())
        private set
    var kardexDirecto by mutableStateOf<List<Kardex>>(emptyList())
        private set
    var califUnidadesDirecto by mutableStateOf<List<CalifUnidad>>(emptyList())
        private set
    var califFinalDirecto by mutableStateOf<List<CalifFinal>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Controla el mensaje textual informativo durante los procesos de descarga.
    var syncStatus by mutableStateOf<String?>(null)
        private set

    // Transforma los flujos de Room en StateFlows reactivos para la UI de Compose.
    // Mantiene los datos vivos en memoria por 5 segundos ante cambios de configuración o rotación.
    val alumnoLocal = repository.getAlumnoLocal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val cargaLocal = repository.getCargaLocal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val kardexLocal = repository.getKardexLocal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califUnidadesLocal = repository.getCalifUnidadesLocal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val califFinalLocal = repository.getCalifFinalLocal().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var lastUpdateKardex by mutableStateOf("Nunca")
    var lastUpdateCarga by mutableStateOf("Nunca")
    var lastUpdateCalifUnidades by mutableStateOf("Nunca")
    var lastUpdateCalifFinal by mutableStateOf("Nunca")

    // Gestiona el proceso de login. Al ser exitoso, solo descarga el perfil inicial
    // para asentar la cookie en el cliente HTTP y evitar el rechazo por llamadas cruzadas simultáneas.
    fun login(matricula: String, contrasenia: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            loginState = LoginResult.Loading
            val result = repository.login(matricula, contrasenia)

            if (result.isSuccess && result.getOrNull()?.acceso == true) {
                loginState = LoginResult.Success
                onLoginSuccess()
                syncProfile()
            } else {
                isLoading = false
                loginState = LoginResult.Error(result.getOrNull()?.mensaje ?: "Error de credenciales")
            }
        }
    }

    // Lanza una corrutina en segundo plano para sincronizar la información del estudiante.
    fun syncProfile() {
        viewModelScope.launch {
            isLoading = true
            syncStatus = "Sincronizando perfil..."
            val result = repository.syncProfile()
            if (result.isSuccess) {
                val alumno = result.getOrNull()
                if (alumno != null) {
                    alumnoDirecto = alumno
                }
                println("CONSOLA_SICENET: ViewModel detectó sincronización exitosa del perfil.")
            }
            isLoading = false
            syncStatus = null
        }
    }

    // Descarga las materias vigentes, asigna la hora del sistema y actualiza el estado local.
    fun syncCargaAcademica() {
        viewModelScope.launch {
            isLoading = true
            syncStatus = "Sincronizando carga..."
            repository.syncCargaAcademica().onSuccess { materias ->
                cargaDirecta = materias
                lastUpdateCarga = getCurrentTimeStr()
                println("CONSOLA_SICENET: Materias actualizadas en el estado directo: ${materias.size}")
            }.onFailure {
                println("CONSOLA_SICENET: Falló la sincronización de carga académica")
            }
            isLoading = false
            syncStatus = null
        }
    }

    // Sincroniza el Kardex leyendo el parámetro de lineamiento de forma reactiva de Room
    // o del estado directo en memoria, cayendo en un valor por defecto en caso de nulidad.
    fun syncKardex() {
        viewModelScope.launch {
            isLoading = true
            syncStatus = "Sincronizando kardex..."
            val lineamiento = alumnoLocal.value?.lineamiento ?: alumnoDirecto?.lineamiento ?: 3
            repository.syncKardex(lineamiento).onSuccess { kardexList ->
                kardexDirecto = kardexList
                lastUpdateKardex = getCurrentTimeStr()
                println("CONSOLA_SICENET: Kardex synchronized con éxito: ${kardexList.size} materias.")
            }.onFailure {
                println("CONSOLA_SICENET: Error al sincronizar Kardex remotos")
            }
            isLoading = false
            syncStatus = null
        }
    }

    // Sincroniza las calificaciones de las unidades del periodo actual.
    fun syncCalifUnidades() {
        viewModelScope.launch {
            isLoading = true
            syncStatus = "Sincronizando parciales..."
            repository.syncCalifUnidades().onSuccess { unidadesList ->
                califUnidadesDirecto = unidadesList
                lastUpdateCalifUnidades = getCurrentTimeStr()
            }
            isLoading = false
            syncStatus = null
        }
    }

    // Sincroniza las actas finales leyendo de Room el tipo de modalidad educativa real del alumno.
    fun syncCalifFinal() {
        viewModelScope.launch {
            isLoading = true
            syncStatus = "Sincronizando finales..."
            val mod = alumnoLocal.value?.modEducativo ?: alumnoDirecto?.modEducativo ?: 2
            repository.syncCalifFinal(mod).onSuccess { finalesList ->
                califFinalDirecto = finalesList
                lastUpdateCalifFinal = getCurrentTimeStr()
                println("CONSOLA_SICENET: Calificaciones finales sincronizadas con éxito: ${finalesList.size}")
            }.onFailure {
                println("CONSOLA_SICENET: Error al sincronizar calificaciones finales")
            }
            isLoading = false
            syncStatus = null
        }
    }

    // Función auxiliar para formatear la hora del sistema (HH:MM) de forma multiplataforma.
    private fun getCurrentTimeStr(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }

    // Destruye por completo los estados en memoria y delega al repositorio el vaciado de las tablas.
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            alumnoDirecto = null
            cargaDirecta = emptyList()
            kardexDirecto = emptyList()
            califUnidadesDirecto = emptyList()
            califFinalDirecto = emptyList()
            loginState = LoginResult.Idle
        }
    }
}