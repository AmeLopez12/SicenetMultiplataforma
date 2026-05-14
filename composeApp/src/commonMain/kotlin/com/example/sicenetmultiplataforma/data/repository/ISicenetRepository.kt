package com.example.sicenetmultiplataforma.data.repository

import com.example.sicenetmultiplataforma.data.model.*
import kotlinx.coroutines.flow.Flow

interface ISicenetRepository {
    // --- Lógica de Sincronización (Nueva) ---
    suspend fun syncProfile(): Result<Alumno>
    suspend fun syncCargaAcademica(): Result<List<Materia>>
    suspend fun syncKardex(lineamiento: Int): Result<List<Kardex>>
    suspend fun syncCalifUnidades(): Result<List<CalifUnidad>>
    suspend fun syncCalifFinal(modEducativo: Int): Result<List<CalifFinal>>

    // --- API (Remoto) ---
    suspend fun login(matricula: String, contrasenia: String): Result<Login>
    suspend fun getProfileRemote(): Result<Alumno>
    suspend fun getCargaAcademicaRemote(): Result<List<Materia>>
    suspend fun getKardexRemote(lineamiento: Int): Result<List<Kardex>>
    suspend fun getCalifUnidadesRemote(): Result<List<CalifUnidad>>
    suspend fun getCalifFinalRemote(modEducativo: Int): Result<List<CalifFinal>>

    // --- Gestión de Sesión ---
    fun getSessionCookie(): String?
    fun setSessionCookie(cookie: String?)

    // --- DB (Local) ---
    fun getAlumnoLocal(): Flow<Alumno?>
    suspend fun saveAlumnoLocal(alumno: Alumno)

    fun getCargaLocal(): Flow<List<Materia>>
    suspend fun saveCargaLocal(materias: List<Materia>)
    suspend fun getLastCargaUpdate(): Long?

    fun getKardexLocal(): Flow<List<Kardex>>
    suspend fun saveKardexLocal(kardex: List<Kardex>)
    suspend fun getLastKardexUpdate(): Long?

    fun getCalifUnidadesLocal(): Flow<List<CalifUnidad>>
    suspend fun saveCalifUnidadesLocal(califUnidades: List<CalifUnidad>)
    suspend fun getLastCalifUnidadUpdate(): Long?

    fun getCalifFinalLocal(): Flow<List<CalifFinal>>
    suspend fun saveCalifFinalLocal(califFinal: List<CalifFinal>)
    suspend fun getLastCalifFinalUpdate(): Long?

    suspend fun logout()
}