package com.example.sicenetmultiplataforma

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform