package com.example.sicenetmultiplataforma.data.repository

import com.example.sicenet.data.local.CalifFinalDao
import com.example.sicenet.data.local.CalifUnidadDao
import com.example.sicenet.data.local.KardexDao
import com.example.sicenet.data.local.MateriaDao
import com.example.sicenetmultiplataforma.data.local.*
import com.example.sicenetmultiplataforma.data.model.*
import com.example.sicenetmultiplataforma.data.network.SicenetApiService
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class SicenetRepository(
    private val apiService: SicenetApiService,
    private val alumnoDao: AlumnoDao,
    private val materiaDao: MateriaDao,
    private val kardexDao: KardexDao,
    private val califUnidadDao: CalifUnidadDao,
    private val califFinalDao: CalifFinalDao
) : ISicenetRepository {

    private var sessionCookie: String? = null

    // --- Gestión de Sesión ---
    override fun getSessionCookie(): String? = sessionCookie
    override fun setSessionCookie(cookie: String?) {
        sessionCookie = cookie
    }

    // --- Lógica de Sincronización ---

    override suspend fun syncProfile(): Result<Alumno> {
        return getProfileRemote().onSuccess { alumno ->
            saveAlumnoLocal(alumno)
        }
    }

    override suspend fun syncCargaAcademica(): Result<List<Materia>> {
        return getCargaAcademicaRemote().onSuccess { materias ->
            saveCargaLocal(materias)
        }
    }

    override suspend fun syncKardex(lineamiento: Int): Result<List<Kardex>> {
        return getKardexRemote(lineamiento).onSuccess { kardex ->
            saveKardexLocal(kardex)
        }
    }

    override suspend fun syncCalifUnidades(): Result<List<CalifUnidad>> {
        return getCalifUnidadesRemote().onSuccess { unidades ->
            saveCalifUnidadesLocal(unidades)
        }
    }

    override suspend fun syncCalifFinal(modEducativo: Int): Result<List<CalifFinal>> {
        return getCalifFinalRemote(modEducativo).onSuccess { finales ->
            saveCalifFinalLocal(finales)
        }
    }

    // --- API (Remoto) ---

    override suspend fun login(matricula: String, contrasenia: String): Result<Login> {
        return try {
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <accesoLogin xmlns="http://tempuri.org/">
                      <strMatricula>${matricula.trim()}</strMatricula>
                      <strContrasenia>${contrasenia.trim()}</strContrasenia>
                      <tipoUsuario>ALUMNO</tipoUsuario>
                    </accesoLogin>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val response = apiService.acceso(soapBody)
            val body = response.bodyAsText()
            
            if (body.contains("acceso\":true")) {
                // Aquí deberías extraer la cookie de los headers si es necesario
                // O extraer el mensaje del XML/JSON devuelto por Sicenet
                Result.success(Login(acceso = true, mensaje = "Acceso correcto"))
            } else {
                Result.success(Login(acceso = false, mensaje = "Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfileRemote(): Result<Alumno> {
        // Implementación de llamada SOAP y mapeo a Alumno
        return Result.failure(Exception("No implementado aún"))
    }

    override suspend fun getCargaAcademicaRemote(): Result<List<Materia>> {
        return Result.failure(Exception("No implementado aún"))
    }

    override suspend fun getKardexRemote(lineamiento: Int): Result<List<Kardex>> {
        return Result.failure(Exception("No implementado aún"))
    }

    override suspend fun getCalifUnidadesRemote(): Result<List<CalifUnidad>> {
        return Result.failure(Exception("No implementado aún"))
    }

    override suspend fun getCalifFinalRemote(modEducativo: Int): Result<List<CalifFinal>> {
        return Result.failure(Exception("No implementado aún"))
    }

    // --- DB (Local) ---

    override fun getAlumnoLocal(): Flow<Alumno?> = alumnoDao.getAlumno()
    override suspend fun saveAlumnoLocal(alumno: Alumno) = alumnoDao.insertAlumno(alumno.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()))

    override fun getCargaLocal(): Flow<List<Materia>> = materiaDao.getAllMaterias()
    override suspend fun saveCargaLocal(materias: List<Materia>) {
        materiaDao.deleteAll()
        materiaDao.insertAll(materias.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
    }
    override suspend fun getLastCargaUpdate(): Long? = materiaDao.getLastUpdateTime()

    override fun getKardexLocal(): Flow<List<Kardex>> = kardexDao.getAllKardex()
    override suspend fun saveKardexLocal(kardex: List<Kardex>) {
        kardexDao.deleteAll()
        kardexDao.insertAll(kardex.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
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
        califFinalDao.deleteAll()
        califFinalDao.insertAll(califFinal.map { it.copy(lastUpdate = Clock.System.now().toEpochMilliseconds()) })
    }
    override suspend fun getLastCalifFinalUpdate(): Long? = califFinalDao.getLastUpdateTime()

    override suspend fun logout() {
        alumnoDao.deleteAlumno()
        materiaDao.deleteAll()
        kardexDao.deleteAll()
        califUnidadDao.deleteAll()
        califFinalDao.deleteAll()
        sessionCookie = null
    }
}
