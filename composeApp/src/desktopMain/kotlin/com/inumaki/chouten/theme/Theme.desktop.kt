package com.inumaki.chouten.theme

import androidx.compose.runtime.Composable
import com.inumaki.chouten.LocalWindowDimensions

@Composable
actual fun isTablet(): Boolean {
    val windowSize = LocalWindowDimensions.current
    return windowSize.widthDp >= 800 // or whatever threshold you consider
}