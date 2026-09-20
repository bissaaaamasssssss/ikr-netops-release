package com.ikr.ngadirojo

import android.content.Context

object NotesManager {
    private const val PREF = "ikr_notes"
    private const val KEY = "notes_text"

    fun save(context: Context, text: String) {
        SecurePrefs.get(context)
            .edit().putString(KEY, text).apply()
    }

    fun load(context: Context): String {
        return SecurePrefs.get(context)
            .getString(KEY, "") ?: ""
    }
}
