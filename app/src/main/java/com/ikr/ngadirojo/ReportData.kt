package com.ikr.ngadirojo

import java.util.UUID

data class InstallationReport(
    val id: String = UUID.randomUUID().toString(),
    val hariTanggal: String = "",
    val pelanggan: String = "",
    val fat: String = "",
    val olt: String = "",
    val team: String = "",
    val paket: String = "",
    val kabel: String = "",
    val catatan: String = "",
    val phoneNumber: String = "085139207580",
    val photoUri: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
