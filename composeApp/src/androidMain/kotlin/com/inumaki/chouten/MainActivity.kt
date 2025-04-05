package com.inumaki.chouten

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.inumaki.chouten.relay.Relay

class MainActivity : ComponentActivity() {
    private val pickFolderLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let { saveSelectedDirectory(it) }
        }

    private fun saveSelectedDirectory(uri: Uri) {
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putString("selected_directory", uri.toString())
            .apply()
    }

    private fun getSavedDirectory(): Uri? {
        val uriString = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("selected_directory", null)
        return uriString?.let { Uri.parse(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val savedUri = getSavedDirectory()
        if (savedUri == null) {
            // Launch folder picker
            pickFolderLauncher.launch(null)
        }

        Relay.setContext(applicationContext)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}