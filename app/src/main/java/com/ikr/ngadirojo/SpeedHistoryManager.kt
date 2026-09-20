package com.ikr.ngadirojo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SpeedRecord(
    val timestamp: Long,
    val download: Double,
    val upload: Double,
    val ping: Int,
    val serverName: String
) {
    fun dateString(): String {
        val sdf = SimpleDateFormat("dd MMM HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}

object SpeedHistoryManager {
    private const val KEY = "speed_history"

    fun save(context: Context, record: SpeedRecord) {
        try {
            val prefs = SecurePrefs.get(context)
            val existing = prefs.getString(KEY, "[]") ?: "[]"
            val arr = JSONArray(existing)

            val newList = mutableListOf<JSONObject>()
            newList.add(JSONObject().apply {
                put("ts", record.timestamp)
                put("dl", record.download)
                put("ul", record.upload)
                put("pg", record.ping)
                put("sv", record.serverName)
            })

            // Ambil 9 yang lama
            for (i in 0 until minOf(arr.length(), 9)) {
                newList.add(arr.getJSONObject(i))
            }

            val newArr = JSONArray()
            newList.forEach { newArr.put(it) }
            prefs.edit().putString(KEY, newArr.toString()).apply()
        } catch (e: Exception) { }
    }

    fun getAll(context: Context): List<SpeedRecord> {
        val result = mutableListOf<SpeedRecord>()
        try {
            val prefs = SecurePrefs.get(context)
            val json = prefs.getString(KEY, "[]") ?: "[]"
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(
                    SpeedRecord(
                        timestamp = obj.optLong("ts", 0),
                        download = obj.optDouble("dl", 0.0),
                        upload = obj.optDouble("ul", 0.0),
                        ping = obj.optInt("pg", 0),
                        serverName = obj.optString("sv", "Unknown")
                    )
                )
            }
        } catch (e: Exception) { }
        return result
    }

    fun clear(context: Context) {
        SecurePrefs.get(context).edit().remove(KEY).apply()
    }
}
