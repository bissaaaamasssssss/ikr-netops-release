package com.ikr.ngadirojo

import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

var lastServerName: String = "Auto"

val SpeedBg = Color(0xFF0A0F1E)
val SpeedCard = Color(0x991B2942)
val SpeedCardSolid = Color(0xCC1B2942)
val CyanAccent = Color(0xFF00E5FF)
val GreenAccent = Color(0xFF10B981)
val AmberAccent = Color(0xFFF59E0B)
val RedAccent = Color(0xFFEF4444)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFFE0E0E0)

val TextShadowStyle = TextStyle(
    shadow = Shadow(Color(0xDD000000), Offset(0f, 2f), 10f)
)

class SpeedTestActivity : ComponentActivity() {
    override fun onDestroy() {
        super.onDestroy()
        try {
            EffectManager.speak(this, "") // stop TTS
        } catch (e: Exception) { }
    }
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(color = SpeedBg) { SpeedTestScreen() } } }
    }
}

enum class TestPhase { IDLE, PING, DOWNLOAD, UPLOAD, DONE }

@Composable
fun SpeedTestScreen() {
    var currentSpeed by remember { mutableStateOf(0.0) }
    var downloadSpeed by remember { mutableStateOf(0.0) }
    var uploadSpeed by remember { mutableStateOf(0.0) }
    var ping by remember { mutableStateOf(0) }
    var jitter by remember { mutableStateOf(0) }
    var phase by remember { mutableStateOf(TestPhase.IDLE) }
    var progress by remember { mutableStateOf(0f) }
    var statusMsg by remember { mutableStateOf("Siap memulai tes") }
    var showHistory by remember { mutableStateOf(false) }
    val ctxLocal = androidx.compose.ui.platform.LocalContext.current
    var showConfetti by remember { mutableStateOf(false) }
    var lastVibrateSpeed by remember { mutableStateOf(0) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val bgType = SettingsManager.getBgType(context)
    val videoAlpha = if (bgType == "video" || bgType == "video_custom") 1f else 0f
    val imageUri = SettingsManager.getBgUri(context)
    val customVideoUri = SettingsManager.getVideoUri(context)
    val showImage = bgType == "image" && imageUri.isNotEmpty()

    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeed.toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "speed"
    )

    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpeedBg)
                .statusBarsPadding()
        ) {
        // TOPBAR dengan LOGO + TULISAN
        Box(
            modifier = Modifier.fillMaxWidth().background(SpeedBg).padding(vertical = 14.dp, horizontal = 20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_ikr),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("IKR NETOPS", color = CyanAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Speed Test", color = TextGray, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(2.dp).background(
                Brush.horizontalGradient(colors = listOf(Color.Transparent, CyanAccent, Color.Transparent))
            )
        )

        Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0F1E),
                                Color(0xFF1B2942),
                                Color(0xFF0A0F1E)
                            )
                        )
                    )
            ) {
            // Render gambar kalau bgType = "image"
            if (showImage) {
                coil.compose.AsyncImage(
                    model = imageUri,
                    contentDescription = "Background Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        val videoUri = if (bgType == "video_custom" && customVideoUri.isNotEmpty()) {
                            Uri.parse(customVideoUri)
                        } else {
                            Uri.parse("android.resource://${context.packageName}/${R.raw.speed_bg}")
                        }
                        setVideoURI(videoUri)
                        setOnPreparedListener { mp -> mp.isLooping = true; mp.setVolume(0f, 0f); start() }
                        setOnErrorListener { _, _, _ -> true }
                        setZOrderOnTop(false)
                        setZOrderMediaOverlay(false)
                    }
                },
                modifier = Modifier.fillMaxSize().alpha(videoAlpha)
            )

            Box(modifier = Modifier.fillMaxSize().background(Color(0x44000000)))

            // Confetti Effect Overlay
            if (showHistory) {
                SpeedHistoryDialog(
                    records = SpeedHistoryManager.getAll(ctxLocal),
                    onDismiss = { showHistory = false },
                    onClear = { SpeedHistoryManager.clear(ctxLocal); showHistory = false }
                )
            }
            ConfettiEffect(isActive = showConfetti, modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SPEED TEST", color = CyanAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, style = TextShadowStyle)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0x33F59E0B))
                            .clickable { showHistory = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📊", fontSize = 16.sp)
                    }
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 20.dp.toPx()
                        val arcSize = Size(size.width - strokeW, size.height - strokeW)
                        val topLeft = Offset(strokeW / 2, strokeW / 2)
                        drawArc(color = Color(0x66000000), startAngle = 135f, sweepAngle = 270f, useCenter = false,
                            topLeft = topLeft, size = arcSize, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                        val maxScale = 300.0
                        val sweep = ((animatedSpeed / maxScale).coerceIn(0.0, 1.0) * 270.0).toFloat()
                        val activeColor = when (phase) {
                            TestPhase.PING -> AmberAccent
                            TestPhase.UPLOAD -> GreenAccent
                            TestPhase.DOWNLOAD -> EffectManager.getActiveColorForSpeed(currentSpeed)
                            else -> CyanAccent
                        }
                        drawArc(color = activeColor, startAngle = 135f, sweepAngle = sweep, useCenter = false,
                            topLeft = topLeft, size = arcSize, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format("%.2f", currentSpeed), color = TextWhite, fontSize = 48.sp, fontWeight = FontWeight.Bold,
                            style = TextStyle(shadow = Shadow(Color(0xDD000000), Offset(0f, 3f), 12f)))
                        Text("Mbps", color = TextWhite, fontSize = 13.sp, letterSpacing = 2.sp, style = TextShadowStyle)
                        // Nama server
                        if (phase != TestPhase.IDLE && phase != TestPhase.DONE) {
                            Text("· " + lastServerName, color = CyanAccent, fontSize = 10.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            when (phase) {
                                TestPhase.IDLE -> "TEKAN MULAI"
                                TestPhase.PING -> "PING TEST"
                                TestPhase.DOWNLOAD -> "↓ DOWNLOAD"
                                TestPhase.UPLOAD -> "↑ UPLOAD"
                                TestPhase.DONE -> "SELESAI"
                            },
                            color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = TextShadowStyle
                        )
                                // Chip Real-time
                                if (phase == TestPhase.DOWNLOAD || phase == TestPhase.UPLOAD) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    androidx.compose.foundation.layout.Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0x3310B981), androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).background(EmeraldGreen, androidx.compose.foundation.shape.CircleShape))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("Real-time", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(Modifier.weight(1f), "↓", "DOWNLOAD", String.format("%.2f", downloadSpeed), "Mbps",
                        if (downloadSpeed < 10 && phase == TestPhase.DONE) RedAccent else CyanAccent)
                    MetricCard(Modifier.weight(1f), "↑", "UPLOAD", String.format("%.2f", uploadSpeed), "Mbps", GreenAccent)
                    MetricCard(Modifier.weight(1f), "⏱", "PING", if (ping > 0) "$ping" else "--", "ms",
                        when { ping in 1..50 -> GreenAccent; ping in 51..150 -> AmberAccent; ping > 150 -> RedAccent; else -> AmberAccent })
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (ping > 0) {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SpeedCard).padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Jitter: $jitter ms",
                                color = if (jitter > 100) RedAccent else if (jitter > 50) AmberAccent else GreenAccent,
                                fontSize = 11.sp, style = TextShadowStyle)
                            Text("Ping: $ping ms",
                                color = if (ping > 200) RedAccent else if (ping > 100) AmberAccent else GreenAccent,
                                fontSize = 11.sp, style = TextShadowStyle)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            when {
                                ping > 200 || jitter > 100 -> "⚠ Koneksi SANGAT BURUK - Cek WiFi/ISP"
                                ping > 100 || jitter > 50 -> "⚠ Koneksi tidak stabil - Dekatkan ke router"
                                else -> "✓ Koneksi stabil"
                            },
                            color = when {
                                ping > 200 || jitter > 100 -> RedAccent
                                ping > 100 || jitter > 50 -> AmberAccent
                                else -> GreenAccent
                            },
                            fontSize = 10.sp, style = TextShadowStyle
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (phase == TestPhase.DOWNLOAD || phase == TestPhase.UPLOAD) {
                    LinearProgressIndicator(progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = CyanAccent, trackColor = SpeedCardSolid)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        if (phase != TestPhase.IDLE && phase != TestPhase.DONE) return@Button
                        scope.launch {
                            EffectManager.playBeepStart()
                            showConfetti = false
                            lastVibrateSpeed = 0
                            currentSpeed = 0.0; downloadSpeed = 0.0; uploadSpeed = 0.0
                            ping = 0; jitter = 0; progress = 0f
                            phase = TestPhase.PING
                            statusMsg = "Mengukur Ping..."
                            val pr = withContext(Dispatchers.IO) { measurePingTcp() }
                            ping = pr.first; jitter = pr.second
                            currentSpeed = ping.toDouble()
                            if (ping > 500) { statusMsg = "Ping tinggi, cek koneksi..."; delay(800) }
                            phase = TestPhase.DOWNLOAD
                            statusMsg = "Mengukur Download..."
                            progress = 0f
                            downloadSpeed = withContext(Dispatchers.IO) { measureDownloadOnce { cur, prog -> 
                                withContext(Dispatchers.Main.immediate) {
                                currentSpeed = cur
                                progress = prog
                                }
                                val tick = (cur / 5).toInt()
                                if (tick > lastVibrateSpeed) {
                                    lastVibrateSpeed = tick
                                    EffectManager.vibrateTick(ctx)
                                }
                            } }
                            currentSpeed = downloadSpeed
                            phase = TestPhase.UPLOAD
                            statusMsg = "Mengukur Upload..."
                            progress = 0f
                            uploadSpeed = withContext(Dispatchers.IO) { measureUploadOnce { cur, prog -> withContext(Dispatchers.Main.immediate) { currentSpeed = cur; progress = prog } } }
                            currentSpeed = uploadSpeed
                            phase = TestPhase.DONE
                            statusMsg = "Selesai — Tes berhasil"

                            // Auto-save riwayat
                            try {
                                SpeedHistoryManager.save(
                                    ctx,
                                    SpeedRecord(
                                        timestamp = System.currentTimeMillis(),
                                        download = downloadSpeed,
                                        upload = uploadSpeed,
                                        ping = ping,
                                        serverName = lastServerName
                                    )
                                )
                            } catch (e: Exception) { }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = (phase == TestPhase.IDLE || phase == TestPhase.DONE),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent, contentColor = SpeedBg,
                        disabledContainerColor = Color(0xAA334155), disabledContentColor = TextGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        when (phase) {
                            TestPhase.IDLE -> "⚡  MULAI TES"
                            TestPhase.PING -> "MENGUKUR PING..."
                            TestPhase.DOWNLOAD -> "TES DOWNLOAD..."
                            TestPhase.UPLOAD -> "TES UPLOAD..."
                            TestPhase.DONE -> "🔄  TES ULANG"
                        },
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, icon: String, label: String, value: String, unit: String, color: Color) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(SpeedCard).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold, style = TextShadowStyle)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextWhite, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, style = TextShadowStyle)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = TextShadowStyle)
        Text(unit, color = TextWhite, fontSize = 9.sp, style = TextShadowStyle)
    }
}

// ==================== PING TCP ====================
suspend fun measurePingTcp(): Pair<Int, Int> = withContext(Dispatchers.IO) {
    val targets = listOf(Pair("dns.google", 443), Pair("1.1.1.1", 443), Pair("www.google.co.id", 443), Pair("www.telkom.co.id", 80))
    val times = mutableListOf<Long>()
    for ((host, port) in targets) {
        for (attempt in 1..3) {
            try {
                val socket = Socket()
                val start = System.nanoTime()
                socket.connect(InetSocketAddress(host, port), 3000)
                val elapsed = (System.nanoTime() - start) / 1_000_000
                socket.close()
                times.add(elapsed)
            } catch (e: Exception) { }
        }
        if (times.size >= 5) break
    }
    if (times.isEmpty()) return@withContext Pair(0, 0)
    val sorted = times.sorted()
    val filtered = if (sorted.size > 4) sorted.subList(1, sorted.size - 1) else sorted
    val avg = filtered.average().toInt()
    val jitter = if (filtered.size > 1) {
        var sum = 0L
        for (i in 1 until filtered.size) sum += Math.abs(filtered[i] - filtered[i-1])
        (sum / (filtered.size - 1)).toInt()
    } else 0
    Pair(avg, jitter)
}

// ==================== DOWNLOAD - BEST SERVER ====================
suspend fun measureDownloadOnce(onProgress: suspend (Double, Float) -> Unit): Double = withContext(Dispatchers.IO) {
    val servers = listOf(
        Pair("Cloudflare", "https://speed.cloudflare.com/__down?bytes=200000000"),
        Pair("Biznet Jakarta", "http://speedtest.biznetnetworks.com/100MB.bin"),
        Pair("OVH Singapore", "https://sgp.proof.ovh.net/files/1Gb.dat"),
        Pair("Telkom Univ", "http://mirror.telkomuniversity.ac.id/ubuntu-releases/22.04/ubuntu-22.04.4-desktop-amd64.iso")
    )

    // Fase 1: cari server pertama yang berhasil connect (max 4 detik)
    var bestUrl = ""
    var bestName = ""
    for ((name, url) in servers) {
        try {
            val ok = withTimeoutOrNull(4000) {
                try {
                    val urlObj = java.net.URL(url)
                    val conn = urlObj.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.connect()
                    val success = conn.responseCode == 200
                    conn.disconnect()
                    success
                } catch (e: Exception) { false }
            } ?: false
            if (ok) {
                bestUrl = url
                bestName = name
                break
            }
        } catch (e: Exception) { }
    }

    if (bestUrl.isEmpty()) {
        bestUrl = servers[0].second
        bestName = "Cloudflare"
    }
    lastServerName = bestName

    // Fase 2: full test di server terpilih, progress bersih 0->100
    onProgress(0.0, 0f)
    tryDownloadServer(bestUrl, onProgress)
}





private suspend fun tryDownloadServer(testUrl: String, onProgress: suspend (Double, Float) -> Unit): Double = withContext(Dispatchers.IO) {
    val numThreads = 16
    var totalBytes = 0L
    val startTime = System.currentTimeMillis()
    val graceMs = 1500L
    val totalDurationMs = 15000L
    val lock = Object()
    var successCount = 0

    val jobs = (1..numThreads).map {
        async(Dispatchers.IO) {
            try {
                val url = URL(testUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Cache-Control", "no-cache")
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) Chrome/120.0.0.0")
                conn.setRequestProperty("Accept-Encoding", "identity")
                conn.connect()

                if (conn.responseCode != 200) {
                    conn.disconnect()
                    return@async
                }

                synchronized(lock) { successCount++ }

                val input = conn.inputStream
                val buffer = ByteArray(262144) // 256KB buffer
                var bytes = input.read(buffer)
                while (bytes != -1) {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed > totalDurationMs) break
                    synchronized(lock) { if (elapsed > graceMs) totalBytes += bytes }
                    bytes = input.read(buffer)
                    val el = (System.currentTimeMillis() - startTime) / 1000.0
                    if (el > 0.3) {
                        val elapsedActive = (el - graceMs / 1000.0).coerceAtLeast(0.1)
                        val cur = (totalBytes * 8) / 1_000_000.0 / elapsedActive
                        onProgress(cur, (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f))
                    }
                }
                input.close()
                conn.disconnect()
            } catch (e: Exception) { }
        }
    }
    jobs.awaitAll()

    if (successCount == 0) return@withContext 0.0
    val duration = (System.currentTimeMillis() - startTime - graceMs) / 1000.0
    if (duration <= 0) return@withContext 0.0
    (totalBytes * 8) / 1_000_000.0 / duration
}



// ==================== UPLOAD ====================
suspend fun measureUploadOnce(onProgress: suspend (Double, Float) -> Unit): Double = withContext(Dispatchers.IO) {
    val testUrl = "https://speed.cloudflare.com/__up"
    val numThreads = 4
    val chunkSize = 2 * 1024 * 1024
    var totalBytes = 0L
    val startTime = System.currentTimeMillis()
    val totalDurationMs = 20000L
    val graceMs = 1500L
    val lock = Object()
    val jobs = (1..numThreads).map {
        async(Dispatchers.IO) {
            try {
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed > totalDurationMs) break
                    val url = URL(testUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"; conn.doOutput = true
                    conn.connectTimeout = 5000; conn.readTimeout = 5000
                    conn.setRequestProperty("Content-Type", "application/octet-stream")
                    conn.setRequestProperty("Accept-Encoding", "identity")
                    conn.setFixedLengthStreamingMode(chunkSize)
                    val payload = ByteArray(chunkSize)
                    val out = conn.outputStream
                    out.write(payload); out.flush(); out.close()
                    conn.responseCode; conn.disconnect()
                    synchronized(lock) { if (elapsed > graceMs) totalBytes += chunkSize }
                    val el = (System.currentTimeMillis() - startTime) / 1000.0
                    if (el > 0.5) {
                        val elapsedActive = (el - graceMs / 1000.0).coerceAtLeast(0.1)
                        val cur = (totalBytes * 8) / 1_000_000.0 / elapsedActive
                        onProgress(cur, (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f))
                    }
                }
            } catch (e: Exception) { }
        }
    }
    jobs.awaitAll()
    val duration = (System.currentTimeMillis() - startTime - graceMs) / 1000.0
    if (duration <= 0) return@withContext 0.0
    (totalBytes * 8) / 1_000_000.0 / duration
}

@Composable
fun SpeedHistoryDialog(
    records: List<SpeedRecord>,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = CyanAccent,
        textContentColor = TextWhite,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Riwayat Speed Test", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            if (records.isEmpty()) {
                Text("Belum ada riwayat. Jalankan tes dulu.", color = TextWhite, fontSize = 13.sp)
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(records.size) { idx ->
                        val r = records[idx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🕐", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(r.dateString(), color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("· " + r.serverName, color = TextGray, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("⬇ ", fontSize = 12.sp)
                                    Text(String.format("%.2f", r.download), color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(" Mbps", color = TextWhite, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("⬆ ", fontSize = 12.sp)
                                    Text(String.format("%.2f", r.upload), color = GreenAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(" Mbps", color = TextWhite, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("📶 ", fontSize = 11.sp)
                                    Text("${r.ping}", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(" ms", color = TextWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClear, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))) {
                Text("HAPUS SEMUA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = TextGray)) {
                Text("TUTUP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    )
}
