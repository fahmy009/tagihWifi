package com.example.tagihinternet.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import com.example.tagihinternet.data.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object UpdateManager {
    private const val GITHUB_VERSION_URL = "https://raw.githubusercontent.com/username/repository/main/version.json"
    private const val GITHUB_RELEASE_URL = "https://github.com/username/repository/releases/latest"

    @Composable
    fun CheckUpdate(currentVersionCode: Int) {
        var showDialog by remember { mutableStateOf(false) }
        var latestVersionCode by remember { mutableIntStateOf(currentVersionCode) }
        val context = androidx.compose.ui.platform.LocalContext.current

        LaunchedEffect(Unit) {
            try {
                // Sederhananya, Anda bisa mengecek file JSON di GitHub yang berisi version code terbaru
                // Untuk sementara, kita hanya siapkan logikanya
                /*
                val response = withContext(Dispatchers.IO) { URL(GITHUB_VERSION_URL).readText() }
                // Parsing JSON untuk mendapatkan version code
                // latestVersionCode = parseJson(response).versionCode
                if (latestVersionCode > currentVersionCode) {
                    showDialog = true
                }
                */
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Update Tersedia") },
                text = { Text("Versi terbaru aplikasi sudah tersedia di GitHub. Silakan unduh untuk fitur terbaru.") },
                confirmButton = {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASE_URL))
                        context.startActivity(intent)
                        showDialog = false
                    }) {
                        Text("UNDUH")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("NANTI")
                    }
                }
            )
        }
    }
}
