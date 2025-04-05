package com.inumaki.chouten

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.inumaki.chouten.relay.Relay
import java.lang.System.setProperty

fun main() = application {
    setProperty("apple.awt.application.name", "Chouten")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Chouten",
    ) {
        App()
    }
}