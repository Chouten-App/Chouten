package com.inumaki.chouten.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad

@Composable
actual fun isTablet(): Boolean {
    return UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad
}