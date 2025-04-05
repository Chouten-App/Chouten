package com.inumaki.chouten

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.inumaki.chouten.features.DiscoverView
import com.inumaki.chouten.relay.Relay
import com.inumaki.chouten.theme.ChoutenTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    Relay.init()

    ChoutenTheme {
        Scaffold(
            backgroundColor = ChoutenTheme.colors.background,
            contentColor = ChoutenTheme.colors.fg
        ) {
            DiscoverView()
        }
    }
}