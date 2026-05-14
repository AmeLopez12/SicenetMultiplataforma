package com.example.sicenetmultiplataforma.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Login(
    val acceso: Boolean = false,
    val mensaje: String = "",
    val cookie: String? = null
)
