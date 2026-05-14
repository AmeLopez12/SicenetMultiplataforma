package com.example.sicenetmultiplataforma.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SicenetApiService(private val client: HttpClient) {

    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"

    /**
     * Función genérica para realizar peticiones SOAP al Sicenet.
     * Reemplaza la lógica de los @Headers y @POST de Retrofit.
     */
    private suspend fun soapRequest(
        soapAction: String,
        cookie: String?,
        soapBody: String
    ): HttpResponse {
        return client.post(baseUrl) {
            header("SOAPAction", soapAction)
            header("Content-Type", "text/xml; charset=utf-8")
            // Si hay cookie, la inyectamos manualmente
            cookie?.let { header("Cookie", it) }
            setBody(soapBody)
        }
    }

    suspend fun acceso(soapBody: String): HttpResponse {
        return soapRequest(
            soapAction = "\"http://tempuri.org/accesoLogin\"",
            cookie = null, // En login aún no tenemos cookie
            soapBody = soapBody
        )
    }

    suspend fun getProfile(cookie: String?, soapBody: String): HttpResponse {
        return soapRequest(
            soapAction = "\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"",
            cookie = cookie,
            soapBody = soapBody
        )
    }

    suspend fun getCargaAcademica(cookie: String?, soapBody: String): HttpResponse {
        return soapRequest(
            soapAction = "\"http://tempuri.org/getCargaAcademicaByAlumno\"",
            cookie = cookie,
            soapBody = soapBody
        )
    }

    suspend fun getKardex(cookie: String?, soapBody: String): HttpResponse {
        return soapRequest(
            soapAction = "\"http://tempuri.org/getAllKardexConPromedioByAlumno\"",
            cookie = cookie,
            soapBody = soapBody
        )
    }

    suspend fun getCalifUnidades(cookie: String?, soapBody: String): HttpResponse {
        return soapRequest(
            soapAction = "\"http://tempuri.org/getCalifUnidadesByAlumno\"",
            cookie = cookie,
            soapBody = soapBody
        )
    }

    suspend fun getCalifFinal(cookie: String?, soapBody: String): HttpResponse {
        return soapRequest(
            soapAction = "\"http://tempuri.org/getAllCalifFinalByAlumnos\"",
            cookie = cookie,
            soapBody = soapBody
        )
    }
}