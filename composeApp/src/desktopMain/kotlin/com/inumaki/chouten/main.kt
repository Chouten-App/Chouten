package com.inumaki.chouten

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import coil3.ImageLoader
import com.inumaki.chouten.relay.Relay
import com.inumaki.relaywasm.WasmRuntime
import java.awt.Desktop
import java.awt.Frame
import java.lang.System.setProperty
import java.awt.Window as AwtWindow
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

data class WindowDimensions(val widthDp: Float, val heightDp: Float)

val LocalWindowDimensions = staticCompositionLocalOf { WindowDimensions(0f, 0f) }

fun main() {
    setProperty("apple.awt.application.name", "Chouten")

    application {
        var isSubmenuShowing by remember { mutableStateOf(false) }
        var useCustomWindow by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }

        val state = rememberWindowState(
            width = 400.dp,
            height = 1500.dp,
        )

        val density = LocalDensity.current
        val widthDp = state.size.width.value
        val heightDp = state.size.height.value

        if (Desktop.isDesktopSupported() &&
            Desktop.getDesktop().isSupported(Desktop.Action.APP_ABOUT)
        ) {
            Desktop.getDesktop().setAboutHandler {
                showAboutDialog = true
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Chouten",
            state = state,
            transparent = useCustomWindow,
            // Transparent window must be undecorated
            undecorated = useCustomWindow,
        ) {
            /*
            MenuBar {
                Menu("File", mnemonic = 'F') {
                    Item("Copy", onClick = { })
                    Item("Paste", onClick = { })
                }
                Menu("Actions", mnemonic = 'A') {
                    CheckboxItem(
                        "Advanced settings",
                        checked = isSubmenuShowing,
                        onCheckedChange = {
                            isSubmenuShowing = !isSubmenuShowing
                        }
                    )
                    if (isSubmenuShowing) {
                        Menu("Settings") {
                            Item("Setting 1", onClick = { })
                            Item("Setting 2", onClick = { })
                        }
                    }
                }
            }
            */
            CompositionLocalProvider(
                LocalWindowDimensions provides WindowDimensions(widthDp, heightDp)
            ) {
                if (useCustomWindow) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                        // Window with rounded corners
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        App(WasmRuntime())
                    }
                } else {
                    App(WasmRuntime())
                }
            }
        }

        if (showAboutDialog) {
            Window(
                onCloseRequest = { showAboutDialog = false },
                title = "About Chouten"
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .height(450.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Chouten")
                    Spacer(Modifier.height(8.dp))
                    Text("Version 1.0.0", color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("A powerful multimedia app built with Compose Multiplatform.")
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
