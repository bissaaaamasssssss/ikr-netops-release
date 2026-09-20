package com.ikr.ngadirojo

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ApkDownloader {
    private const val FILE_NAME = "IKR-NETOPS-update.apk"

    fun start(context: Context, apkUrl: String, onStatus: (String) -> Unit) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), FILE_NAME)
            if (file.exists()) file.delete()

            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("Update IKR NETOPS")
                .setDescription("Mengunduh versi terbaru...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, FILE_NAME)
                .setMimeType("application/vnd.android.package-archive")

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val id = dm.enqueue(request)
            Toast.makeText(context, "Download dimulai...", Toast.LENGTH_SHORT).show()
            onStatus("Downloading")

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == id) {
                        onStatus("Completed")
                        installApk(context)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= 33) {
                context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
        } catch (e: Exception) {
            onStatus("Error: ${e.message}")
            Toast.makeText(context, "Gagal download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun installApk(context: Context) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), FILE_NAME)
            if (!file.exists()) {
                Toast.makeText(context, "File APK tidak ditemukan", Toast.LENGTH_LONG).show()
                return
            }
            val uri = if (Build.VERSION.SDK_INT >= 24) {
                FileProvider.getUriForFile(context, "com.ikr.ngadirojo.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal install: ${e.message}\nCek izin Install Unknown Apps", Toast.LENGTH_LONG).show()
        }
    }
}
