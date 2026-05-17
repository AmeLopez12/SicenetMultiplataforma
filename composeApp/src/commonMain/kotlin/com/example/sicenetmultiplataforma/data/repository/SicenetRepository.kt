package com.example.sicenetmultiplataforma.data.repository

import com.example.sicenetmultiplataforma.data.local.*
import com.example.sicenetmultiplataforma.data.model.*
import com.example.sicenetmultiplataforma.data.network.SicenetApiService
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*

class SicenetRepository(
    private val apiService: SicenetApiService,
    private val alumnoDao: AlumnoDao,
    private val materiaDao: MateriaDao,
    private val kardexDao: KardexDao,
    private val califUnidadDao: CalifUnidadDao,
    private val califFinalDao: CalifFinalDao
) : ISicenetRepository {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private var sessionCookie: String? = null

    override fun getSessionCookie(): String? = sessionCookie
    override fun setSessionCookie(cookie: String?) {
        sessionCookie = cookie
    }

    // Procesa el XML crudo del Web Service para extraer la cadena JSON interna.
    // Reemplaza los caracteres de escape XML por comillas y símbolos legibles.
    private fun extractResult(xml: String, methodName: String): String {
        val tag = "${methodName}Result"
        if (xml.isBlank()) {
            println("CONSOLA_SICENET: El servidor regresó un string totalmente vacío.")
            return ""
        }

        if (!xml.contains(tag)) {
            println("CONSOLA_SICENET: ALERTA -> No se encontró '$tag'. Respuesta cruda del servidor:\n$xml")
            return ""
        }

        return xml.substringAfter(tag)
            .substringAfter(">")
            .substringBefore("</")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .trim()
    }

    // --- Funciones de Sincronización: Conectan la API remota con la base de datos local ---

    override suspend fun syncProfile(): Result<Alumno> {
        return getProfileRemote().onSuccess { saveAlumnoLocal(it) }
    }

    override suspend fun syncCargaAcademica(): Result<List<Materia>> {
        return getCargaAcademicaRemote().onSuccess { saveCargaLocal(it) }
    }

    override suspend fun syncKardex(lineamiento: Int): Result<List<Kardex>> {
        return getKardexRemote(lineamiento).onSuccess { saveKardexLocal(it) }
    }

    override suspend fun syncCalifUnidades(): Result<List<CalifUnidad>> {
        return getCalifUnidadesRemote().onSuccess { saveCalifUnidadesLocal(it) }
    }

    override suspend fun syncCalifFinal(modEducativo: Int): Result<List<CalifFinal>> {
        return getCalifFinalRemote(modEducativo).onSuccess { saveCalifFinalLocal(it) }
    }

    // --- Consultas a la API Remota y Extracción de Datos (Red) ---

    override suspend fun login(matricula: String, contrasenia: String): Result<Login> {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><accesoLogin xmlns="http://tempuri.org/"><strMatricula>${matricula.trim()}</strMatricula><strContrasenia>${contrasenia.trim()}</strContrasenia><tipoUsuario>ALUMNO</tipoUsuario></accesoLogin></soap:Body></soap:Envelope>"""
            val response = apiService.acceso(soapBody)
            val body = response.bodyAsText()

            // Intercepta la cabecera Set-Cookie para capturar y guardar el identificador de sesión.
            val cookieHeader = response.headers["Set-Cookie"]
            if (cookieHeader != null) {
                val cleanCookie = cookieHeader.substringBefore(";")
                setSessionCookie(cleanCookie)
            }

            if (body.contains("\"acceso\":true") || body.contains("<acceso>true</acceso>")) {
                Result.success(Login(acceso = true, mensaje = "Éxito", cookie = getSessionCookie()))
            } else {
                Result.success(Login(acceso = false, mensaje = "Credenciales incorrectas"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getProfileRemote(): Result<Alumno> {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response = apiService.getProfile(sessionCookie, soapBody)
            val jsonContent = extractResult(response.bodyAsText(), "getAlumnoAcademicoWithLineamiento")
            println("CONSOLA_SICENET: JSON Perfil Extraído -> $jsonContent")
            if (jsonContent.isNotBlank()) {
                Result.success(Alumno.fromJson(jsonContent))
            } else Result.failure(Exception("Sin datos de perfil"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCargaAcademicaRemote(): Result<List<Materia>> {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getCargaAcademicaByAlumno xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response = apiService.getCargaAcademica(sessionCookie, soapBody)
            val xmlString = response.bodyAsText()

            val jsonContent = extractResult(xmlString, "getCargaAcademicaByAlumno")
            println("CONSOLA_SICENET: JSON Carga Extraído -> $jsonContent")

            if (jsonContent.isNotBlank()) {
                val jsonElement = Json.parseToJsonElement(jsonContent)
                val jsonArray = when (jsonElement) {
                    is JsonArray -> jsonElement
                    is JsonObject -> jsonElement.values.firstOrNull() as? JsonArray
                    else -> null
                }

                val list = mutableListOf<Materia>()

                // Mapeador dinámico: Evalúa múltiples nombres de llaves para evitar campos vacíos.
                jsonArray?.forEach { element ->
                    val obj = element.jsonObject

                    val nombreMateria = obj["materia"]?.jsonPrimitive?.content
                        ?: obj["Materia"]?.jsonPrimitive?.content
                        ?: obj["nombre"]?.jsonPrimitive?.content ?: ""

                    val claveOficial = obj["clvOficial"]?.jsonPrimitive?.content
                        ?: obj["ClvOficial"]?.jsonPrimitive?.content
                        ?: obj["clave"]?.jsonPrimitive?.content ?: ""

                    val grupoMateria = obj["grupo"]?.jsonPrimitive?.content
                        ?: obj["Grupo"]?.jsonPrimitive?.content ?: ""

                    val docenteMateria = obj["docente"]?.jsonPrimitive?.content
                        ?: obj["Docente"]?.jsonPrimitive?.content ?: ""

                    val creditosStr = obj["creditosMateria"]?.jsonPrimitive?.content
                        ?: obj["CreditosMateria"]?.jsonPrimitive?.content
                        ?: obj["creditos"]?.jsonPrimitive?.content ?: "0"

                    list.add(
                        Materia(
                            clave = claveOficial,
                            nombre = nombreMateria,
                            grupo = grupoMateria,
                            docente = docenteMateria,
                            creditos = creditosStr.toIntOrNull() ?: 0,
                            lunes = obj["lunes"]?.jsonPrimitive?.content ?: obj["Lunes"]?.jsonPrimitive?.content ?: "",
                            martes = obj["martes"]?.jsonPrimitive?.content ?: obj["Martes"]?.jsonPrimitive?.content ?: "",
                            miercoles = obj["miercoles"]?.jsonPrimitive?.content ?: obj["Miercoles"]?.jsonPrimitive?.content ?: "",
                            jueves = obj["jueves"]?.jsonPrimitive?.content ?: obj["Jueves"]?.jsonPrimitive?.content ?: "",
                            viernes = obj["viernes"]?.jsonPrimitive?.content ?: obj["Viernes"]?.jsonPrimitive?.content ?: ""
                        )
                    )
                }

                println("CONSOLA_SICENET: Mapeo completado. Enviando ${list.size} materias a Room.")
                Result.success(list)
            } else {
                Result.failure(Exception("Sin datos de carga"))
            }
        } catch (e: Exception) {
            println("CONSOLA_SICENET: Error fatal en el mapeo de Carga -> ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getKardexRemote(lineamiento: Int): Result<List<Kardex>> {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/"><aluLineamiento>$lineamiento</aluLineamiento></getAllKardexConPromedioByAlumno></soap:Body></soap:Envelope>"""

            val response = apiService.getKardex(sessionCookie, soapBody)
            val xmlString = response.bodyAsText()

            val jsonContent = extractResult(xmlString, "getAllKardexConPromedioByAlumno")
            println("CONSOLA_SICENET: JSON Kardex Extraído -> $jsonContent")

            if (jsonContent.isNotBlank()) {
                val jsonElement = Json.parseToJsonElement(jsonContent)
                val jsonArray = when (jsonElement) {
                    is JsonArray -> jsonElement
                    is JsonObject -> jsonElement.values.firstOrNull() as? JsonArray
                    else -> null
                }

                val list = mutableListOf<Kardex>()

                jsonArray?.forEach { element ->
                    val obj = element.jsonObject

                    val clv = obj["ClvOfiMat"]?.jsonPrimitive?.content
                        ?: obj["ClvMat"]?.jsonPrimitive?.content
                        ?: obj["clave"]?.jsonPrimitive?.content ?: ""

                    val mat = obj["Materia"]?.jsonPrimitive?.content
                        ?: obj["materia"]?.jsonPrimitive?.content ?: ""

                    val cal = obj["Calif"]?.jsonPrimitive?.content
                        ?: obj["Calificacion"]?.jsonPrimitive?.content
                        ?: obj["calificacion"]?.jsonPrimitive?.content ?: "0"

                    val p1 = obj["P1"]?.jsonPrimitive?.content ?: ""
                    val a1 = obj["A1"]?.jsonPrimitive?.content ?: ""
                    val per = if (p1.isNotBlank() && a1.isNotBlank()) "$p1 $a1" else {
                        obj["Periodo"]?.jsonPrimitive?.content ?: obj["periodo"]?.jsonPrimitive?.content ?: ""
                    }

                    list.add(
                        Kardex(
                            clave = clv,
                            materia = mat,
                            calificacion = cal,
                            periodo = per
                        )
                    )
                }

                println("CONSOLA_SICENET: Kardex guardado en Room exitosamente con ${list.size} registros.")
                Result.success(list)
            } else {
                Result.failure(Exception("Sin datos de kardex"))
            }
        } catch (e: Exception) {
            println("CONSOLA_SICENET: Error fatal en el mapeo de Kardex -> ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCalifUnidadesRemote(): Result<List<CalifUnidad>> {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getCalifUnidadesByAlumno xmlns="http://tempuri.org/" /></soap:Body></soap:Envelope>"""
            val response = apiService.getCalifUnidades(sessionCookie, soapBody)
            val jsonContent = extractResult(response.bodyAsText(), "getCalifUnidadesByAlumno")
            println("CONSOLA_SICENET: JSON Unidades Extraído -> $jsonContent")

            if (jsonContent.isNotBlank()) {
                Result.success(CalifUnidad.fromJsonList(jsonContent))
            } else Result.failure(Exception("Sin datos de unidades"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCalifFinalRemote(modEducativo: Int): Result<List<CalifFinal>> {
        return try {
            val soapBody = """<?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><getAllCalifFinalByAlumnos xmlns="http://tempuri.org/"><modEducativo>$modEducativo</modEducativo></getAllCalifFinalByAlumnos></soap:Body></soap:Envelope>"""
            val response = apiService.getCalifFinal(sessionCookie, soapBody)
            val jsonContent = extractResult(response.bodyAsText(), "getAllCalifFinalByAlumnos")
            println("CONSOLA_SICENET: JSON Finales Extraído -> $jsonContent")

            if (jsonContent.isNotBlank()) {
                val jsonElement = Json.parseToJsonElement(jsonContent)
                val jsonArray = when (jsonElement) {
                    is JsonArray -> jsonElement
                    is JsonObject -> jsonElement.values.firstOrNull() as? JsonArray
                    else -> null
                }

                val list = mutableListOf<CalifFinal>()

                jsonArray?.forEach { element ->
                    val obj = element.jsonObject

                    val mat = obj["Materia"]?.jsonPrimitive?.content
                        ?: obj["materia"]?.jsonPrimitive?.content
                        ?: obj["nombre"]?.jsonPrimitive?.content ?: ""

                    val califStr = obj["Calif"]?.jsonPrimitive?.content
                        ?: obj["Calificacion"]?.jsonPrimitive?.content
                        ?: obj["calificacion"]?.jsonPrimitive?.content ?: "0"

                    val acred = obj["Acred"]?.jsonPrimitive?.content
                        ?: obj["Acreditacion"]?.jsonPrimitive?.content
                        ?: obj["acreditacion"]?.jsonPrimitive?.content ?: ""

                    list.add(
                        CalifFinal(
                            materia = mat,
                            calificacion = califStr.toIntOrNull() ?: 0,
                            acreditacion = acred
                        )
                    )
                }

                println("CONSOLA_SICENET: Calificaciones Finales guardadas en Room con ${list.size} registros.")
                Result.success(list)
            } else {
                Result.failure(Exception("Sin datos de calificaciones finales"))
            }
        } catch (e: Exception) {
            println("CONSOLA_SICENET: Error fatal en Finales -> ${e.message}")
            Result.failure(e)
        }
    }

    // Analizador genérico auxiliar para procesar colecciones JSON anidadas o indirectas.
    private inline fun <reified T> parseResilientList(jsonString: String): List<T> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            val element = json.parseToJsonElement(jsonString)
            val jsonArray = when (element) {
                is JsonArray -> element
                is JsonObject -> {
                    val firstValue = element.values.firstOrNull()
                    when (firstValue) {
                        is JsonArray -> firstValue
                        is JsonPrimitive -> if (firstValue.isString) json.parseToJsonElement(firstValue.content) as? JsonArray else null
                        else -> null
                    }
                }
                else -> null
            }
            if (jsonArray != null) json.decodeFromJsonElement<List<T>>(jsonArray) else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // --- Persistencia Local (SQLite/Room): Métodos de almacenamiento y lectura ---

    override fun getAlumnoLocal(): Flow<Alumno?> = alumnoDao.getAlumno()
    override suspend fun saveAlumnoLocal(alumno: Alumno) {
        if (alumno.matricula.isNotBlank()) {
            try {
                alumnoDao.deleteAlumno()
                // Adjunta una estampa de tiempo actual Unix antes de realizar la inserción.
                val alumnoConTimestamp = alumno.copy(lastUpdate = Clock.System.now().toEpochMilliseconds())
                alumnoDao.insertAlumno(alumnoConTimestamp)
                println("CONSOLA_SICENET: ¡Alumno guardado con éxito en Room! -> ${alumnoConTimestamp.nombre}")
            } catch (e: Exception) {
                println("CONSOLA_SICENET: Error al insertar Alumno en Room -> ${e.message}")
            }
        }
    }

    override fun getCargaLocal(): Flow<List<Materia>> = materiaDao.getAllMaterias()
    override suspend fun saveCargaLocal(materias: List<Materia>) {
        try {
            materiaDao.deleteAll()
            materiaDao.insertAll(materias.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
            println("CONSOLA_SICENET: Mapeo completado. Enviando ${materias.size} materias a Room.")
        } catch (e: Exception) {
            println("CONSOLA_SICENET: Error al insertar Carga en Room -> ${e.message}")
        }
    }

    override suspend fun getLastCargaUpdate(): Long? = materiaDao.getLastUpdateTime()

    override fun getKardexLocal(): Flow<List<Kardex>> = kardexDao.getAllKardex()
    override suspend fun saveKardexLocal(kardex: List<Kardex>) {
        try {
            kardexDao.deleteAll()
            kardexDao.insertAll(kardex.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
            println("CONSOLA_SICENET: Kardex guardado en Room exitosamente con ${kardex.size} registros.")
        } catch (e: Exception) {
            println("CONSOLA_SICENET: Error al guardar Kardex en Room -> ${e.message}")
        }
    }

    override suspend fun getLastKardexUpdate(): Long? = kardexDao.getLastUpdateTime()

    override fun getCalifUnidadesLocal(): Flow<List<CalifUnidad>> = califUnidadDao.getAllCalifUnidades()
    override suspend fun saveCalifUnidadesLocal(califUnidades: List<CalifUnidad>) {
        califUnidadDao.deleteAll()
        califUnidadDao.insertAll(califUnidades.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
    }

    override suspend fun getLastCalifUnidadUpdate(): Long? = califUnidadDao.getLastUpdateTime()

    override fun getCalifFinalLocal(): Flow<List<CalifFinal>> = califFinalDao.getAllCalifFinal()
    override suspend fun saveCalifFinalLocal(califFinal: List<CalifFinal>) {
        try {
            califFinalDao.deleteAll()
            califFinalDao.insertAll(califFinal.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
            println("CONSOLA_SICENET: Calificaciones Finales guardadas en Room con ${califFinal.size} registros.")
        } catch (e: Exception) {
            println("CONSOLA_SICENET: Error al guardar Finales en Room -> ${e.message}")
        }
    }

    override suspend fun getLastCalifFinalUpdate(): Long? = califFinalDao.getLastUpdateTime()

    // Limpia por completo todas las tablas locales y destruye la cookie para cerrar la sesión.
    override suspend fun logout() {
        alumnoDao.deleteAlumno()
        materiaDao.deleteAll()
        kardexDao.deleteAll()
        califUnidadDao.deleteAll()
        califFinalDao.deleteAll()
        sessionCookie = null
    }
}