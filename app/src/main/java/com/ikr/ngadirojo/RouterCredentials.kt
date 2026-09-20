package com.ikr.ngadirojo

import android.util.Base64

object RouterCredentials {
    private const val K = "IKR@Netops2026#Key"

    private fun decode(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        val result = StringBuilder()
        for (i in bytes.indices) {
            result.append((bytes[i].toInt() xor K[i % K.length].code).toChar())
        }
        return result.toString()
    }

    val YINET_USER: String get() = decode("KC8/KSA=")
    val YINET_PASS: String get() = decode("CC8/KSBUR1xR")
    val FIBER_USER: String get() = decode("KC8/KSA=")
    val FIBER_PASS: String get() = decode("bHsuBnEtNAlREVdCWnkQLg==")
}
