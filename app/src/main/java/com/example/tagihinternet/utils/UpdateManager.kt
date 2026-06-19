package com.example.tagihinternet.utils

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object UpdateManager {
    // URL Raw ke file version.json di repository Anda
    private const val GITHUB_VERSION_URL = "https://raw.githubusercontent.com/fahmy009/tagihWIfi/main/version.json"
    private const val GITHUB_RELEASE_URL = "https://github.com/fahmy009/tagihWIfi/releases/latest"

    @Composable
    fun CheckUpdate(currentVersionCode: Int) {
        var showDialog by remember { mutableStateOf(false) }
        var downloadUrl by remember { mutableStateOf("") }
        var releaseNotes by remember { mutableStateOf("") }
        val context = androidx.compose.ui.platform.LocalContext.current

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val response = URL(GITHUB_VERSION_URL).readText()
                    val json = JSONObject(response)
                    val latestVersionCode = json.getInt("versionCode")
                    val url = json.getString("downloadUrl")
                    val notes = json.optString("releaseNotes", "Versi terbaru tersedia.")

                    if (latestVersionCode > currentVersionCode) {
                        downloadUrl = url
                        releaseNotes = notes
                        showDialog = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Update Tersedia") },
                text = { Text(releaseNotes) },
                confirmButton = {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                        context.startActivity(intent)
                        showDialog = false
                    }) {
                        Text("UNDUH SEKARANG")
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
