package com.example.sicenetmultiplataforma

import androidx.compose.ui.window.ComposeUIViewController
import com.example.sicenetmultiplataforma.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
