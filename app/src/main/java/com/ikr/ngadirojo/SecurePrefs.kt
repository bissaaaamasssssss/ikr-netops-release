package com.ikr.ngadirojo

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {
    private var instance: SharedPreferences? = null

    fun get(context: Context): SharedPreferences {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    val masterKey = MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    instance = EncryptedSharedPreferences.create(
                        context,
                        "ikr_secure_prefs",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                }
            }
        }
        return instance!!
    }
}
