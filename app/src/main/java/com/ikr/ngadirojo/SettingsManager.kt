package com.ikr.ngadirojo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SettingsManager {
    private const val PREF = "ikr_settings"
    private const val KEY_LIGHT_MODE = "light_mode"
    private const val KEY_LIGHT_MODE_USER_SET = "light_mode_user_set"
    private const val KEY_VIDEO_BG = "video_bg"
    private const val KEY_BG_TYPE = "bg_type"  // "video", "image", "off"
    private const val KEY_BG_URI = "bg_uri"
    private const val KEY_VIDEO_URI = "video_uri"
    private const val KEY_HISTORY = "speed_history"

    // Light mode
    fun isLightMode(context: Context): Boolean {
        val prefs = SecurePrefs.get(context)
        val userSet = prefs.getBoolean(KEY_LIGHT_MODE_USER_SET, false)
        if (userSet) {
            return prefs.getBoolean(KEY_LIGHT_MODE, false)
        }
        // Belum set manual → ikut sistem
        val uiMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isDarkSystem = uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        return !isDarkSystem
    }

    fun isLightModeUserSet(context: Context): Boolean {
        return SecurePrefs.get(context).getBoolean(KEY_LIGHT_MODE_USER_SET, false)
    }

    fun resetToSystem(context: Context) {
        SecurePrefs.get(context).edit().remove(KEY_LIGHT_MODE_USER_SET).apply()
    }

    fun setLightMode(context: Context, enabled: Boolean) {
        SecurePrefs.get(context)
            .edit()
            .putBoolean(KEY_LIGHT_MODE, enabled)
            .putBoolean(KEY_LIGHT_MODE_USER_SET, true)
            .apply()
    }

    // Background type: "video", "image", "off"
    fun getBgType(context: Context): String {
        return SecurePrefs.get(context)
            .getString(KEY_BG_TYPE, "video") ?: "video"
    }

    fun setBgType(context: Context, type: String) {
        SecurePrefs.get(context)
            .edit().putString(KEY_BG_TYPE, type).apply()
    }

    // Background URI (kalau type = "image")
    fun getBgUri(context: Context): String {
        return SecurePrefs.get(context)
            .getString(KEY_BG_URI, "") ?: ""
    }

    fun setBgUri(context: Context, uri: String) {
        SecurePrefs.get(context)
            .edit().putString(KEY_BG_URI, uri).apply()
    }

    // Speed history
    fun saveSpeedResult(context: Context, download: Double, upload: Double, ping: Int) {
        val prefs = SecurePrefs.get(context)
        val existing = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        try {
            val arr = JSONArray(existing)
            val entry = JSONObject().apply {
                put("d", download)
                put("u", upload)
                put("p", ping)
                put("t", System.currentTimeMillis())
            }
            val newArr = JSONArray()
            newArr.put(entry)
            for (i in 0 until minOf(2, arr.length())) {
                newArr.put(arr.get(i))
            }
            prefs.edit().putString(KEY_HISTORY, newArr.toString()).apply()
        } catch (e: Exception) { }
    }

    fun getHistory(context: Context): List<Triple<Double, Double, Int>> {
        val prefs = SecurePrefs.get(context)
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val result = mutableListOf<Triple<Double, Double, Int>>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(Triple(obj.getDouble("d"), obj.getDouble("u"), obj.getInt("p")))
            }
        } catch (e: Exception) { }
        return result
    }

    // Video selection: 1, 2, 3
    fun getVideoIndex(context: Context): Int {
        return SecurePrefs.get(context)
            .getInt("video_index", 1)
    }

    fun setVideoIndex(context: Context, index: Int) {
        SecurePrefs.get(context)
            .edit().putInt("video_index", index).apply()
    }

    // Video URI custom (dari galeri)
    fun getVideoUri(context: Context): String {
        return SecurePrefs.get(context)
            .getString(KEY_VIDEO_URI, "") ?: ""
    }

    fun setVideoUri(context: Context, uri: String) {
        SecurePrefs.get(context)
            .edit().putString(KEY_VIDEO_URI, uri).apply()
    }
}
