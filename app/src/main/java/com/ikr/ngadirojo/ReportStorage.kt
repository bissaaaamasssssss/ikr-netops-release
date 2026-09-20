package com.ikr.ngadirojo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ReportStorage {
    private const val PREF = "ikr_reports"
    private const val KEY = "reports"

    fun save(context: Context, report: InstallationReport) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY, "[]") ?: "[]"
        try {
            val arr = JSONArray(existing)
            val newArr = JSONArray()
            val obj = JSONObject().apply {
                put("id", report.id)
                put("hariTanggal", report.hariTanggal)
                put("pelanggan", report.pelanggan)
                put("fat", report.fat)
                put("olt", report.olt)
                put("team", report.team)
                put("paket", report.paket)
                put("kabel", report.kabel)
                put("catatan", report.catatan)
                put("timestamp", report.timestamp)
            }
            newArr.put(obj)
            for (i in 0 until minOf(49, arr.length())) {
                newArr.put(arr.get(i))
            }
            prefs.edit().putString(KEY, newArr.toString()).apply()
        } catch (e: Exception) { }
    }

    fun getAll(context: Context): List<InstallationReport> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, "[]") ?: "[]"
        val result = mutableListOf<InstallationReport>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(InstallationReport(
                    id = o.getString("id"),
                    hariTanggal = o.getString("hariTanggal"),
                    pelanggan = o.getString("pelanggan"),
                    fat = o.getString("fat"),
                    olt = o.getString("olt"),
                    team = o.getString("team"),
                    paket = o.getString("paket"),
                    kabel = o.getString("kabel"),
                    catatan = o.getString("catatan"),
                    timestamp = o.getLong("timestamp")
                ))
            }
        } catch (e: Exception) { }
        return result
    }

    fun delete(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY, "[]") ?: "[]"
        try {
            val arr = JSONArray(existing)
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.getString("id") != id) newArr.put(o)
            }
            prefs.edit().putString(KEY, newArr.toString()).apply()
        } catch (e: Exception) { }
    }
}
