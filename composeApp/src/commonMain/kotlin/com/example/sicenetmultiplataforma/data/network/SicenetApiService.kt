package com.example.sicenetmultiplataforma.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SicenetApiService(private val client: HttpClient) {

    // Endpoint unificado del Web Service de Sicenet para el control de alumnos.
    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"

    // Interceptor centralizado para estructurar el protocolo SOAP sobre HTTP POST.
    private suspend fun soapRequest(
        soapAction: String,
        cookie: String?,
        soapBody: String
    ): HttpResponse {
        return client.post(baseUrl) {
            // Cabecera obligatoria en servidores .NET (ASMX) para identificar la función a ejecutar.
            header("SOAPAction", soapAction)
            header("Content-Type", "text/xml; charset=utf-8")

            // Inyecta manualmente el identificador de sesión para mantener el estado de autenticación.
            cookie?.let { header("Cookie", it) }

            // Envía el sobre de XML correspondiente a la solicitud.
            setBody(soapBody)
        }
    }

    suspend fun acceso(soapBody: String): HttpResponse =
        soapRequest("http://tempuri.org/accesoLogin", null, soapBody)

    suspend fun getProfile(cookie: String?, soapBody: String): HttpResponse =
        soapRequest("http://tempuri.org/getAlumnoAcademicoWithLineamiento", cookie, soapBody)

    suspend fun getCargaAcademica(cookie: String?, soapBody: String): HttpResponse =
        soapRequest("http://tempuri.org/getCargaAcademicaByAlumno", cookie, soapBody)

    suspend fun getKardex(cookie: String?, soapBody: String): HttpResponse =
        soapRequest("http://tempuri.org/getAllKardexConPromedioByAlumno", cookie, soapBody)

    suspend fun getCalifUnidades(cookie: String?, soapBody: String): HttpResponse =
        soapRequest("http://tempuri.org/getCalifUnidadesByAlumno", cookie, soapBody)

    suspend fun getCalifFinal(cookie: String?, soapBody: String): HttpResponse =
        soapRequest("http://tempuri.org/getAllCalifFinalByAlumnos", cookie, soapBody)
}
