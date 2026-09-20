package com.ikr.ngadirojo

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import java.util.Locale

object EffectManager {
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("id", "ID")
                    ttsReady = true
                }
            }
        }
    }

    fun speak(context: Context, text: String) {
        initTts(context)
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ikr_tts")
        }
    }

    fun playBeepSuccess() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tone.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    tone.release()
                }, 300)
            }, 250)
        } catch (e: Exception) { }
    }

    fun playBeepStart() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tone.release()
            }, 200)
        } catch (e: Exception) { }
    }

    fun vibrateSuccess(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 100, 100, 100, 100, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) { }
    }

    fun vibrateTick(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) { }
    }

    fun getSpeedAnnouncement(speed: Double): String {
        return when {
            speed >= 200 -> "WOW! Kecepatan luar biasa! ${speed.toInt()} mega bit per second"
            speed >= 100 -> "Kecepatan sangat cepat! ${speed.toInt()} mega bit per second"
            speed >= 50 -> "Kecepatan bagus! ${speed.toInt()} mega bit per second"
            speed >= 20 -> "Kecepatan standar, ${speed.toInt()} mega bit per second"
            speed > 0 -> "Kecepatan lambat, ${speed.toInt()} mega bit per second. Periksa jaringan"
            else -> "Tes gagal"
        }
    }

    fun getActiveColorForSpeed(speed: Double): androidx.compose.ui.graphics.Color {
        return when {
            speed >= 200 -> androidx.compose.ui.graphics.Color(0xFF00E5FF) // Cyan Neon
            speed >= 100 -> androidx.compose.ui.graphics.Color(0xFF10B981) // Emerald
            speed >= 50 -> androidx.compose.ui.graphics.Color(0xFF84CC16)  // Lime
            speed >= 20 -> androidx.compose.ui.graphics.Color(0xFFF59E0B)  // Amber
            speed > 0 -> androidx.compose.ui.graphics.Color(0xFFEF4444)    // Red
            else -> androidx.compose.ui.graphics.Color(0xFF00E5FF)
        }
    }
}
