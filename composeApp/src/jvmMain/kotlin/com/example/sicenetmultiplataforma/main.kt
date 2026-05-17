package com.example.sicenetmultiplataforma

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sicenetmultiplataforma.di.initKoin
import com.example.sicenetmultiplataforma.App

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SicenetMultiplataforma",
        ) {
            App()
        }
    }
}
