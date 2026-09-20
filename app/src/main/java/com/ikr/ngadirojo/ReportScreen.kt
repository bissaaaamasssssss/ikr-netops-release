package com.ikr.ngadirojo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Calendar

fun getCurrentDate(): String {
    val cal = Calendar.getInstance()
    val hariArr = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    val hari = hariArr[cal.get(Calendar.DAY_OF_WEEK) - 1]
    val tgl = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
    val bln = String.format("%02d", cal.get(Calendar.MONTH) + 1)
    val thn = cal.get(Calendar.YEAR)
    return "$hari, $tgl/$bln/$thn"
}

fun formatReportText(r: InstallationReport): String {
    return """
        *LAPORAN PEMASANGAN*
        ━━━━━━━━━━━━━━━━━━━
        
        TANGGAL : ${r.hariTanggal.uppercase()}
        PELANGGAN : ${r.pelanggan.uppercase()}
        FAT : ${r.fat.uppercase()}
        OLT : ${r.olt.uppercase()}
        TEAM : ${r.team.uppercase()}
        PAKET : ${r.paket.uppercase()}
        KABEL : ${r.kabel.uppercase()}
        CATATAN : ${r.catatan.uppercase()}
        
        
        
        
        ━━━━━━━━━━━━━━━━━━━
    """.trimIndent()
}

@Composable
fun ReportScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var pelanggan by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var olt by remember { mutableStateOf("") }
    var team by remember { mutableStateOf("") }
    var paket by remember { mutableStateOf("Starlit 200 Mbps") }
    var paketExpanded by remember { mutableStateOf(false) }
    val paketOptions = listOf(
        "Starlit 100 Mbps",
        "Starlit 200 Mbps",
        "Starlit 300 Mbps",
        "Starlit 500 Mbps",
        "Starlit 1 Gbps",
        "Custom"
    )
    var kabel by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("085139207580") }
    var photoUri by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var showWaDialog by remember { mutableStateOf(false) }
    val hariTanggal = getCurrentDate()  // OTOMATIS, TIDAK BISA DIEDIT

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
            Toast.makeText(context, "Foto dipilih ✓", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog Pilih Paket
    if (paketExpanded) {
        PaketDialog(
            currentPaket = paket,
            options = paketOptions,
            onSelect = { paket = it },
            onDismiss = { paketExpanded = false }
        )
    }

    // Dialog pilih tujuan WA
    if (showWaDialog) {
        WaTargetDialog(
            context = context,
            report = InstallationReport(
                hariTanggal = hariTanggal,
                pelanggan = pelanggan,
                fat = fat,
                olt = olt,
                team = team,
                paket = paket,
                kabel = kabel,
                catatan = catatan,
                phoneNumber = phoneNumber,
                photoUri = photoUri
            ),
            onDismiss = { showWaDialog = false }
        )
    }

    if (showHistory) {
        HistoryDialog(
            context = context,
            onDismiss = { showHistory = false },
            onLoadReport = { r ->
                pelanggan = r.pelanggan
                fat = r.fat
                olt = r.olt
                team = r.team
                paket = r.paket.ifBlank { "Starlit 200 Mbps" }
                kabel = r.kabel
                catatan = r.catatan
                phoneNumber = r.phoneNumber
                photoUri = r.photoUri
                showHistory = false
                Toast.makeText(context, "Laporan dimuat ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog Pilih Tujuan WA
    

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkBg2)))
            .verticalScroll(rememberScrollState())
    ) {
        // TOPBAR
        Box(
            modifier = Modifier.fillMaxWidth().background(DarkBackground).padding(vertical = 14.dp, horizontal = 20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_ikr),
                    contentDescription = "Logo",
                    modifier = Modifier.size(36.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("LAPORAN", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Teknisi IKR Ngadirojo", color = TextSub, fontSize = 10.sp)
                }
                IconButton(onClick = { showHistory = true }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_report),
                        contentDescription = "Riwayat",
                        modifier = Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(Amber)
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(2.dp).background(
                Brush.horizontalGradient(colors = listOf(Color.Transparent, NeonCyan, Color.Transparent))
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TANGGAL OTOMATIS (TAMPIL SAJA)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📅", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Tanggal Otomatis", color = TextSub, fontSize = 10.sp)
                    Text(hariTanggal, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FORM INPUT
        SimpleField("Pelanggan", pelanggan, R.drawable.ic_person) { pelanggan = it }
        SimpleField("FAT", fat, R.drawable.ic_satellite) { fat = it }
        SimpleField("OLT", olt, R.drawable.ic_antenna) { olt = it }
        SimpleField("Team", team, R.drawable.ic_team) { team = it }
        PaketButtonField(paket) { paketExpanded = true }
        SimpleField("Kabel", kabel, R.drawable.ic_cable) { kabel = it }
        SimpleField("Catatan", catatan, R.drawable.ic_note) { catatan = it }

        Spacer(modifier = Modifier.height(10.dp))

        // FOTO
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable {
                imagePicker.launch("image/*")
            },
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(2.dp, if (photoUri.isNotEmpty()) EmeraldGreen else BorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (photoUri.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            .clip(CircleShape).background(Color(0xCC000000)).padding(6.dp)
                    ) {
                        Text("✏️", fontSize = 12.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Foto",
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Tambah Foto", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // NOMOR WA
        // Card CS Starlit (info saja, bukan input)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x1A25D366)),
            border = BorderStroke(1.5.dp, Color(0xFF25D366)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF25D366)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("CS STARLIT", color = Color(0xFF25D366), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(phoneNumber, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text("✓", color = Color(0xFF25D366), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PREVIEW
        ReportPreviewCard(
            InstallationReport(
                hariTanggal = hariTanggal,
                pelanggan = pelanggan,
                fat = fat,
                olt = olt,
                team = team,
                paket = paket,
                kabel = kabel,
                catatan = catatan,
                phoneNumber = phoneNumber,
                photoUri = photoUri
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ACTION BUTTONS
        ReportActionBar(
            context = context,
            report = InstallationReport(
                hariTanggal = hariTanggal,
                pelanggan = pelanggan,
                fat = fat,
                olt = olt,
                team = team,
                paket = paket,
                kabel = kabel,
                catatan = catatan,
                phoneNumber = phoneNumber,
                photoUri = photoUri
            ),
            onShowWaDialog = { showWaDialog = true },
            onSave = {
                if (pelanggan.isBlank()) {
                    Toast.makeText(context, "Pelanggan wajib diisi", Toast.LENGTH_SHORT).show()
                } else {
                    ReportStorage.save(context, InstallationReport(
                        hariTanggal = hariTanggal,
                        pelanggan = pelanggan,
                        fat = fat,
                        olt = olt,
                        team = team,
                        paket = paket,
                        kabel = kabel,
                        catatan = catatan,
                        phoneNumber = phoneNumber,
                        photoUri = photoUri
                    ))
                    Toast.makeText(context, "Laporan disimpan ✓", Toast.LENGTH_SHORT).show()
                }
            },
            onClear = {
                pelanggan = ""
                fat = ""
                olt = ""
                team = ""
                paket = "Starlit 200 Mbps"
                kabel = ""
                catatan = ""
                photoUri = ""
                Toast.makeText(context, "Form dibersihkan", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SimpleField(
    label: String,
    value: String,
    iconRes: Int,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = TextSub, fontSize = 11.sp) },
        leadingIcon = {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(NeonCyan)
            )
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = TextSub,
            cursorColor = NeonCyan,
            focusedTextColor = TextMain,
            unfocusedTextColor = TextMain
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

// ==================== PREVIEW CARD HIJAU ====================
@Composable
fun ReportPreviewCard(report: InstallationReport) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
        border = BorderStroke(2.dp, EmeraldGreen),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("LAPORAN PEMASANGAN", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(10.dp))

            PreviewRow("📅 Tanggal", report.hariTanggal)
            PreviewRow("👤 Pelanggan", report.pelanggan)
            PreviewRow("🛰️ FAT", report.fat)
            PreviewRow("📡 OLT", report.olt)
            PreviewRow("👥 Team", report.team)
            PreviewRow("📦 Paket", report.paket)
            PreviewRow("🔗 Kabel", report.kabel)
            PreviewRow("📝 Catatan", report.catatan)

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("✓ Siap dikirim", color = Color.White, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
fun PreviewRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, modifier = Modifier.width(110.dp))
            Text(": $value", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ==================== DIALOG RIWAYAT ====================
@Composable
fun HistoryDialog(
    context: Context,
    onDismiss: () -> Unit,
    onLoadReport: (InstallationReport) -> Unit
) {
    val reports = remember { ReportStorage.getAll(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📚", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Riwayat Laporan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            if (reports.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada laporan tersimpan", color = TextSub, fontSize = 12.sp)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    reports.forEach { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onLoadReport(r) },
                            colors = CardDefaults.cardColors(containerColor = DarkBg2),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.pelanggan.ifBlank { "Tanpa Nama" }, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(r.hariTanggal, color = TextSub, fontSize = 10.sp)
                                    if (r.paket.isNotBlank()) Text("Paket: ${r.paket}", color = TextSub, fontSize = 10.sp)
                                }
                                IconButton(onClick = {
                                    ReportStorage.delete(context, r.id)
                                    Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("🗑️", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                Text("TUTUP", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ==================== ACTION BAR ====================



// ==================== WA TARGET DIALOG ====================
@Composable
fun WaTargetDialog(
    context: Context,
    report: InstallationReport,
    onDismiss: () -> Unit
) {
    val csStarlitNumber = "085139207580"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\uD83D\uDCE4", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Kirim Laporan Ke", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                        onDismiss()
                        sendWaTo(context, report, csStarlitNumber)
                    },
                    colors = CardDefaults.cardColors(containerColor = DarkBg2),
                    border = BorderStroke(1.5.dp, EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_starlite),
                                contentDescription = "Starlite",
                                modifier = Modifier.size(44.dp).clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CS STARLIT", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(csStarlitNumber, color = TextSub, fontSize = 11.sp)
                        }
                        Text("\u203A", color = NeonCyan, fontSize = 22.sp)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                        onDismiss()
                        openWhatsAppPicker(context, report)
                    },
                    colors = CardDefaults.cardColors(containerColor = DarkBg2),
                    border = BorderStroke(1.5.dp, Amber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Amber),
                            contentAlignment = Alignment.Center
                        ) { Text("\uD83D\uDCF1", fontSize = 20.sp) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NOMOR LAIN", color = Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Pilih dari kontak WhatsApp", color = TextSub, fontSize = 11.sp)
                        }
                        Text("\u203A", color = Amber, fontSize = 22.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSub)) {
                Text("BATAL")
            }
        }
    )
}

// ==================== WA SEND HELPERS ====================
fun sendWaTo(context: Context, report: InstallationReport, targetNumber: String) {
    val text = formatReportText(report)
    try {
        // KUNCI: Kalau ada foto, pakai ACTION_SEND dengan foto + teks
        if (report.photoUri.isNotEmpty()) {
            try {
                val photoUri = android.net.Uri.parse(report.photoUri)
                val mimeType = context.contentResolver.getType(photoUri) ?: "image/*"
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, photoUri)
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp")
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback: tanpa foto
            }
        }
        
        // Kalau tidak ada foto, kirim teks saja via wa.me
        val waNumber = targetNumber.replace("+", "").replace(" ", "").replace("-", "")
        val waNumberIntl = if (waNumber.startsWith("0")) "62" + waNumber.substring(1) else waNumber
        val url = "https://wa.me/" + waNumberIntl + "?text=" + Uri.encode(text)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Laporan", text))
        Toast.makeText(context, "Teks disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }
}

fun openWhatsAppPicker(context: Context, report: InstallationReport) {
    val text = formatReportText(report)
    try {
        // Kalau ada foto → kirim foto + teks
        if (report.photoUri.isNotEmpty()) {
            try {
                val photoUri = android.net.Uri.parse(report.photoUri)
                val mimeType = context.contentResolver.getType(photoUri) ?: "image/*"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, photoUri)
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp")
                }
                context.startActivity(Intent.createChooser(intent, "Pilih Kontak WhatsApp"))
                return
            } catch (e: Exception) { }
        }
        // Tanpa foto → teks saja
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        context.startActivity(Intent.createChooser(intent, "Pilih Kontak WhatsApp"))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Laporan", text))
        Toast.makeText(context, "WhatsApp tidak terinstall. Teks disalin.", Toast.LENGTH_SHORT).show()
    }
}

// ==================== REPORT ACTION BAR ====================
@Composable
fun ReportActionBar(
    context: Context,
    report: InstallationReport,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onShowWaDialog: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Button(
            onClick = onShowWaDialog,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = "WA",
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("KIRIM KE WHATSAPP", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Laporan", formatReportText(report)))
                    Toast.makeText(context, "Teks disalin", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).height(46.dp),
                border = BorderStroke(1.5.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("\uD83D\uDCCB SALIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = DarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("\uD83D\uDCBE SIMPAN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = TextSub)
        ) {
            Text("\uD83D\uDDD1\uFE0F  BERSIHKAN FORM", fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}




// ==================== PAKET BUTTON + DIALOG ====================
@Composable
fun PaketButtonField(
    paket: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = paket,
        onValueChange = { },
        readOnly = true,
        label = { Text("Paket", color = TextSub, fontSize = 11.sp) },
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_package),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(NeonCyan)
            )
        },
        trailingIcon = {
            Text("▼", color = NeonCyan, fontSize = 12.sp)
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onClick() },
        singleLine = true,
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = NeonCyan,
            disabledLabelColor = NeonCyan,
            disabledLeadingIconColor = NeonCyan,
            disabledTrailingIconColor = NeonCyan,
            disabledTextColor = TextMain,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun PaketDialog(
    currentPaket: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_package),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(NeonCyan)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Pilih Paket", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column {
                options.forEach { option ->
                    val isSelected = option == currentPaket
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            onSelect(option)
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0x3300E5FF) else DarkBg2
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeonCyan else BorderColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                option,
                                color = if (isSelected) NeonCyan else TextMain,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text("✓", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSub)) {
                Text("TUTUP")
            }
        }
    )
}
