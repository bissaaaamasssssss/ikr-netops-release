package com.ikr.ngadirojo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class SpeedHistory(
    val id: Int,
    val timestamp: String,
    val download: String,
    val upload: String,
    val ping: String,
    val server: String
)

class SpeedHistoryDatabase(context: Context) : SQLiteOpenHelper(context, "SpeedHistory.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE speed_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT,
                download TEXT,
                upload TEXT,
                ping TEXT,
                server TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS speed_history")
        onCreate(db)
    }

    fun addRecord(timestamp: String, download: String, upload: String, ping: String, server: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("timestamp", timestamp)
            put("download", download)
            put("upload", upload)
            put("ping", ping)
            put("server", server)
        }
        db.insert("speed_history", null, values)
        db.close()
    }

    fun getAllRecords(): List<SpeedHistory> {
        val list = mutableListOf<SpeedHistory>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM speed_history ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            list.add(
                SpeedHistory(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                )
            )
        }
        cursor.close()
        db.close()
        return list
    }

    fun deleteAll() {
        val db = writableDatabase
        db.execSQL("DELETE FROM speed_history")
        db.close()
    }
}
