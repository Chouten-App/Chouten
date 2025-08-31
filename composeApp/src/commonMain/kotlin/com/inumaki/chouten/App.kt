package com.inumaki.chouten

import androidx.compose.runtime.Composable
import com.inumaki.chouten.features.App.AppView
import com.inumaki.relaywasm.WasmRuntime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    wasmRuntime: WasmRuntime
) {
    AppView(
        wasmRuntime = wasmRuntime
    )
}