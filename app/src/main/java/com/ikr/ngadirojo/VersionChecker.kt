package com.ikr.ngadirojo

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val apkUrl: String,
    val mandatory: Boolean
)

object VersionChecker {
    const val VERSION_URL = "https://raw.githubusercontent.com/hakerindonesia413-ai/ikr-netops-release/main/version.json"

    fun checkUpdate(currentVersionCode: Int, callback: (VersionInfo?) -> Unit) {
        Thread {
            try {
                val conn = URL(VERSION_URL).openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.setRequestProperty("Cache-Control", "no-cache")
                conn.connect()
                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val obj = JSONObject(json)
                    val info = VersionInfo(
                        versionCode = obj.getInt("versionCode"),
                        versionName = obj.getString("versionName"),
                        changelog = obj.getString("changelog"),
                        apkUrl = obj.getString("apkUrl"),
                        mandatory = obj.optBoolean("mandatory", false)
                    )
                    Handler(Looper.getMainLooper()).post {
                        if (info.versionCode > currentVersionCode) callback(info)
                        else callback(null)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post { callback(null) }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { callback(null) }
            }
        }.start()
    }

    fun getCurrentVersionCode(context: android.content.Context): Int {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt()
            else @Suppress("DEPRECATION") info.versionCode
        } catch (e: Exception) { 1 }
    }
}
