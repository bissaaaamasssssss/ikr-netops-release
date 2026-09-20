package com.ikr.ngadirojo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Warna aktif (di-update dari MainContainer)
var DarkBackground = Color(0xFF0A0F1E)
var DarkBg2 = Color(0xFF111C33)
var SurfaceDark = Color(0xFF1B2942)
var SurfaceLight = Color(0xFF233655)
val NeonCyan = Color(0xFF00E5FF)
val EmeraldGreen = Color(0xFF10B981)
val Amber = Color(0xFFF59E0B)
var TextMain = Color(0xFFF8FAFC)
var TextSub = Color(0xFF94A3B8)
var BorderColor = Color(0xFF2D4A7C)

// Set warna berdasarkan Light/Dark
fun updateThemeColors(isLight: Boolean) {
    if (isLight) {
        DarkBackground = Color(0xFFF8FAFC)
        DarkBg2 = Color(0xFFE2E8F0)
        SurfaceDark = Color(0xFFFFFFFF)
        SurfaceLight = Color(0xFFF1F5F9)
        TextMain = Color(0xFF0F172A)
        TextSub = Color(0xFF64748B)
        BorderColor = Color(0xFFCBD5E1)
    } else {
        DarkBackground = Color(0xFF0A0F1E)
        DarkBg2 = Color(0xFF111C33)
        SurfaceDark = Color(0xFF1B2942)
        SurfaceLight = Color(0xFF233655)
        TextMain = Color(0xFFF8FAFC)
        TextSub = Color(0xFF94A3B8)
        BorderColor = Color(0xFF2D4A7C)
    }
}

// Warna Light Mode
val LightBackground = Color(0xFFF8FAFC)
val LightBg2 = Color(0xFFE2E8F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceLight = Color(0xFFF1F5F9)
val LightText = Color(0xFF0F172A)
val LightTextSub = Color(0xFF64748B)
val LightBorder = Color(0xFFCBD5E1)

fun applyTheme(isLight: Boolean) {
    if (isLight) {
        DarkBackground = LightBackground
        DarkBg2 = LightBg2
        SurfaceDark = LightSurface
        SurfaceLight = LightSurfaceLight
        TextMain = LightText
        TextSub = LightTextSub
        BorderColor = LightBorder
    } else {
        DarkBackground = Color(0xFF0A0F1E)
        DarkBg2 = Color(0xFF111C33)
        SurfaceDark = Color(0xFF1B2942)
        SurfaceLight = Color(0xFF233655)
        TextMain = Color(0xFFF8FAFC)
        TextSub = Color(0xFF94A3B8)
        BorderColor = Color(0xFF2D4A7C)
    }
}

enum class DialogType { NONE, ABOUT, DEVELOPER, DONATE }
enum class AppScreen { HOME, REPORT, SATRIA, SETTINGS }

data class RouterOption(
    val name: String,
    val iconRes: Int,
    val ip: String,
    val user: String,
    val pass: String,
    val autoLogin: Boolean,
    val isManual: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                    MainContainer()
                }
            }
        }
    }

    @Composable
    fun MainContainer() {
        val context = LocalContext.current
        // State tema yang bisa di-refresh
        var isLightMode by remember { mutableStateOf(SettingsManager.isLightMode(context)) }
        // Update warna global saat tema berubah
        LaunchedEffect(isLightMode) {
            updateThemeColors(isLightMode)
        }
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        var showManualDialog by remember { mutableStateOf(false) }
        var selectedRouter by remember { mutableStateOf<RouterOption?>(null) }
        var activeDialog by remember { mutableStateOf(DialogType.NONE) }

        val routers = listOf(
            RouterOption("YINET", R.drawable.ic_router, "http://192.168.1.1", "admin", "Admin123!", true),
            RouterOption("FIBER HOME", R.drawable.ic_router, "http://192.168.1.1/html/login_inter.html", "admin", "%0|F?H@f!berhO3e", true),
            RouterOption("MODEM LAIN MANUAL", R.drawable.ic_router, "", "", "", false, isManual = true)
        )

        if (showManualDialog) {
            ManualInputDialog(
                onDismiss = { showManualDialog = false },
                onConfirm = { ip, user, pw ->
                    val intent = Intent(this, RouterWebView::class.java).apply {
                        putExtra("ROUTER_IP", ip)
                        putExtra("ROUTER_USER", user)
                        putExtra("ROUTER_PASS", pw)
                        putExtra("AUTO_LOGIN", false)
                    }
                    startActivity(intent)
                    showManualDialog = false
                }
            )
        }

        when (activeDialog) {
            DialogType.ABOUT -> AboutDialog(onDismiss = { activeDialog = DialogType.NONE })
            DialogType.DEVELOPER -> DeveloperDialog(onDismiss = { activeDialog = DialogType.NONE })
            DialogType.DONATE -> DonateDialog(context = context, onDismiss = { activeDialog = DialogType.NONE })
            DialogType.NONE -> {}
        }

        Scaffold(
            containerColor = DarkBackground,
            bottomBar = {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onHomeClick = { currentScreen = AppScreen.HOME },
                    onReportClick = { currentScreen = AppScreen.REPORT },
                    onSatriaClick = { currentScreen = AppScreen.SATRIA },
                    onSpeedClick = { startActivity(Intent(this@MainActivity, SpeedTestActivity::class.java)) },
                    onSettingsClick = { currentScreen = AppScreen.SETTINGS }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        routers = routers,
                        selectedRouter = selectedRouter,
                        onRouterSelected = { r ->
                            selectedRouter = r
                            if (r.isManual) showManualDialog = true
                        },
                        onOpenRouter = { router ->
                            if (router.isManual) {
                                showManualDialog = true
                            } else {
                                val intent = Intent(this@MainActivity, RouterWebView::class.java).apply {
                                    putExtra("ROUTER_IP", router.ip)
                                    putExtra("ROUTER_USER", router.user)
                                    putExtra("ROUTER_PASS", router.pass)
                                    putExtra("AUTO_LOGIN", router.autoLogin)
                                }
                                startActivity(intent)
                            }
                        }
                    )
                    AppScreen.REPORT -> ReportScreen()
                    AppScreen.SATRIA -> SatriaScreen()
                    AppScreen.SETTINGS -> SettingsScreen(
                        onAboutClick = { activeDialog = DialogType.ABOUT },
                        onDeveloperClick = { activeDialog = DialogType.DEVELOPER },
                        onDonateClick = { activeDialog = DialogType.DONATE },
                            onReportClick = { currentScreen = AppScreen.REPORT },
                            onSatriaClick = { currentScreen = AppScreen.SATRIA }
                        )
                }
            }
        }
    }

    @Composable
    fun BottomNavBar(
        currentScreen: AppScreen,
        onHomeClick: () -> Unit,
        onReportClick: () -> Unit,
        onSatriaClick: () -> Unit,
        onSpeedClick: () -> Unit,
        onSettingsClick: () -> Unit
    ) {
        Surface(color = SurfaceDark, shadowElevation = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().height(88.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(R.drawable.ic_home, "HOME", currentScreen == AppScreen.HOME, false, onHomeClick)
                BottomNavItem(R.drawable.ic_speed, "TES SPEED", false, true, onSpeedClick)
                BottomNavItem(R.drawable.ic_settings, "SETTING", currentScreen == AppScreen.SETTINGS, false, onSettingsClick)
            }
        }
    }

    @Composable
    fun BottomNavItem(iconRes: Int, label: String, isSelected: Boolean, isSpecial: Boolean, onClick: () -> Unit) {
        val tintColor = when {
            isSpecial -> EmeraldGreen
            isSelected -> NeonCyan
            else -> TextSub
        }
        val glowColor = when {
            isSpecial -> Color(0x2210B981)
            isSelected -> Color(0x2200E5FF)
            else -> Color.Transparent
        }
        Column(
            modifier = Modifier.width(68.dp).clip(RoundedCornerShape(14.dp)).clickable { onClick() }.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(glowColor), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = iconRes), contentDescription = label,
                    modifier = Modifier.size(24.dp), colorFilter = ColorFilter.tint(tintColor))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = tintColor, fontSize = 10.sp,
                fontWeight = if (isSelected || isSpecial) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.5.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape)
                .background(if (isSelected || isSpecial) tintColor else Color.Transparent))
        }
    }
}

// ==================== HOME SCREEN ====================
@Composable
fun HomeScreen(
    routers: List<RouterOption>,
    selectedRouter: RouterOption?,
    onRouterSelected: (RouterOption) -> Unit,
    onOpenRouter: (RouterOption) -> Unit
) {
    val ctxHome = androidx.compose.ui.platform.LocalContext.current
    var updateInfo by remember { mutableStateOf<VersionInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        VersionChecker.checkUpdate(VersionChecker.getCurrentVersionCode(ctxHome)) { info ->
            if (info != null) {
                updateInfo = info
                showUpdateDialog = true
            }
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            info = updateInfo!!,
            onDismiss = { showUpdateDialog = false },
            onDownload = {
                ApkDownloader.start(ctxHome, updateInfo!!.apkUrl) { status ->
                    if (status == "Completed") showUpdateDialog = false
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkBg2)))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bell Icon Update
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (updateInfo != null) Color(0x33EF4444) else Color(0x22FFFFFF))
                    .clickable { if (updateInfo != null) showUpdateDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 17.sp)
                if (updateInfo != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFFEF4444))
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SurfaceDark, SurfaceLight))),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_ikr),
                contentDescription = "Logo",
                modifier = Modifier.size(94.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("IKR NETOPS", color = NeonCyan, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text("Network Operations System", color = TextSub, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            "PILIH ROUTER",
            color = TextSub,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 10.dp)
        )

        routers.forEach { router ->
            RouterCard(
                router = router,
                isSelected = selectedRouter == router,
                onClick = { onRouterSelected(router) }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = { onOpenRouter(selectedRouter ?: routers[0]) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🚀  BUKA ROUTER", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RouterCard(router: RouterOption, isSelected: Boolean, onClick: () -> Unit) {
    val iconColor = when {
        router.name.contains("YINET", ignoreCase = true) -> Color(0xFF00E5FF)
        router.name.contains("FIBER", ignoreCase = true) -> Color(0xFF8B5CF6)
        router.name.contains("MODEM", ignoreCase = true) -> Color(0xFFF59E0B)
        else -> Color(0xFF00E5FF)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SurfaceDark else Color(0x661B2942)),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) NeonCyan else BorderColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(iconColor, iconColor.copy(alpha = 0.6f)))
                            else Brush.linearGradient(listOf(iconColor.copy(alpha = 0.55f), iconColor.copy(alpha = 0.2f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = router.iconRes),
                        contentDescription = router.name,
                        modifier = Modifier.size(26.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(router.name, color = if (isSelected) NeonCyan else TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                if (router.isManual) {
                    Text("Isi IP, User & PW manual", color = TextSub, fontSize = 10.sp)
                    Text("Tap untuk konfigurasi", color = Amber, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
                } else {
                    Text("IP: ${router.ip.replace("http://", "").split("/")[0]}", color = TextSub, fontSize = 10.sp)
                    Text(if (router.autoLogin) "● Auto-Login" else "● Manual",
                        color = if (router.autoLogin) EmeraldGreen else Amber, fontSize = 10.sp,
                        fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (isSelected) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(NeonCyan), contentAlignment = Alignment.Center) {
                    Text("✓", color = DarkBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
            }

// ==================== SETTINGS SCREEN ====================
@Composable
fun SettingsScreen(
    onAboutClick: () -> Unit,
    onDeveloperClick: () -> Unit,
    onDonateClick: () -> Unit,
    onReportClick: () -> Unit = {},
    onSatriaClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLightMode by remember { mutableStateOf(SettingsManager.isLightMode(context)) }
    var bgType by remember { mutableStateOf(SettingsManager.getBgType(context)) }
    var isSnackbarShown by remember { mutableStateOf(false) }
    var showBgPicker by remember { mutableStateOf(false) }
    var showIkrMenu by remember { mutableStateOf(false) }
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            SettingsManager.setBgUri(context, uri.toString())
            SettingsManager.setBgType(context, "image")
            bgType = "image"
            android.widget.Toast.makeText(context, "Gambar background dipilih", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val videoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Persist URI permission biar awet
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { }
            SettingsManager.setVideoUri(context, uri.toString())
            SettingsManager.setBgType(context, "video_custom")
            bgType = "video_custom"
            android.widget.Toast.makeText(context, "Video custom dipilih", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog pilih background
    if (showBgPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBgPicker = false },
            containerColor = SurfaceDark,
            titleContentColor = NeonCyan,
            textContentColor = TextMain,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎨", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Pilih Background Speed Test", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column {
                    Text("Pilih jenis background untuk halaman Speed Test:",
                        color = TextSub, fontSize = 12.sp, modifier = Modifier.padding(bottom = 14.dp))

                    // Opsi 1: Video default
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            SettingsManager.setBgType(context, "video")
                            bgType = "video"
                            showBgPicker = false
                            android.widget.Toast.makeText(context, "Video Background aktif", android.widget.Toast.LENGTH_SHORT).show()
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = bgType == "video", onClick = {
                            SettingsManager.setBgType(context, "video")
                            bgType = "video"
                            showBgPicker = false
                        }, colors = RadioButtonDefaults.colors(selectedColor = NeonCyan))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("🎬 Video Default", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Video background bawaan aplikasi", color = TextSub, fontSize = 10.sp)
                        }
                    }

                    // Opsi 2: Video dari Galeri (BARU)
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            videoPickerLauncher.launch("video/*")
                            showBgPicker = false
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = bgType == "video_custom", onClick = {
                            videoPickerLauncher.launch("video/*")
                            showBgPicker = false
                        }, colors = RadioButtonDefaults.colors(selectedColor = NeonCyan))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("🎬 Video dari Galeri", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Pilih video sendiri dari HP (MP4)", color = TextSub, fontSize = 10.sp)
                        }
                    }

                    // Opsi 3: Gambar Custom
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            imagePickerLauncher.launch("image/*")
                            showBgPicker = false
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = bgType == "image", onClick = {
                            imagePickerLauncher.launch("image/*")
                            showBgPicker = false
                        }, colors = RadioButtonDefaults.colors(selectedColor = NeonCyan))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("🖼️ Gambar dari Galeri", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Pilih gambar sendiri dari HP", color = TextSub, fontSize = 10.sp)
                        }
                    }

                    // Opsi 3: Off (navy gradient)
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            SettingsManager.setBgType(context, "off")
                            bgType = "off"
                            showBgPicker = false
                            android.widget.Toast.makeText(context, "Background Off (navy solid)", android.widget.Toast.LENGTH_SHORT).show()
                        }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = bgType == "off", onClick = {
                            SettingsManager.setBgType(context, "off")
                            bgType = "off"
                            showBgPicker = false
                        }, colors = RadioButtonDefaults.colors(selectedColor = NeonCyan))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("❌ Nonaktif", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Background navy gradient", color = TextSub, fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBgPicker = false }, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                    Text("TUTUP", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val onLightModeClick = {
        val newValue = !isLightMode
        SettingsManager.setLightMode(context, newValue)
        isLightMode = newValue
        updateThemeColors(newValue)
        android.widget.Toast.makeText(context, 
            if (newValue) "Light Mode aktif — buka ulang app kalau tidak berubah" else "Dark Mode aktif — buka ulang app kalau tidak berubah", 
            android.widget.Toast.LENGTH_SHORT).show()
    }
    val onVideoToggleClick = {
        showBgPicker = true
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkBg2)))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("PENGATURAN", color = NeonCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Text("Kelola preferensi aplikasi", color = TextSub, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(28.dp))
        // ===== KHUSUS IKR (expandable) =====
        SettingsCard(
            R.drawable.ic_team,
            color = Color(0xFF4CAF50), "KHUSUS IKR",
            if (showIkrMenu) "Tap untuk tutup" else "Laporan Teknisi - SATRIA",
            onClick = { showIkrMenu = !showIkrMenu }
        )
        if (showIkrMenu) {
            SettingsCard(R.drawable.ic_report, "Laporan Teknisi", iconColor = Color(0xFF4CAF50), "Buat dan kirim laporan ke CS", onReportClick)
            SettingsCard(R.drawable.ic_satria, "SATRIA", "Buka portal SATRIA 2000", onSatriaClick)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SettingsCard(R.drawable.ic_info, "Tentang Aplikasi", iconColor = Color(0xFF2196F3), "IKR NETOPS v8.1", onAboutClick)
        SettingsCard(R.drawable.ic_developer, "Pengembang", iconColor = Color(0xFF9C27B0), "Themin Vigi Dwi Safik Reno", onDeveloperClick)
        SettingsCard(R.drawable.ic_router, "Server Speed Test", iconColor = Color(0xFFFF9800), "Cloudflare · Tele2 · OVH", {})
        SettingsCard(R.drawable.ic_settings, "Mode Login", iconColor = Color(0xFFE91E63), "Auto-Login · Manual Fallback", {})
        SettingsCard(R.drawable.ic_info, "Versi Aplikasi", iconColor = Color(0xFF9E9E9E), "v8.1 · Build 2", {})
        SettingsCard(R.drawable.ic_settings, "Mode Tampilan", 
            if (isLightMode) "Light Mode aktif" else "Dark Mode aktif", 
            onLightModeClick)
        SettingsCard(R.drawable.ic_speed, "Background Speed Test", 
            when (bgType) {
                "video" -> "🎬 Video Default"
                "video_custom" -> "🎬 Video dari Galeri"
                "image" -> "🖼️ Gambar Custom"
                else -> "❌ Nonaktif"
            }, 
            onVideoToggleClick)
        SettingsCard(R.drawable.ic_donate, "Donasi", "Dukung pengembangan aplikasi", onDonateClick)
        Spacer(modifier = Modifier.height(24.dp))
        Text("© 2026 IKR Ngadirojo", color = TextSub, fontSize = 10.sp, letterSpacing = 1.sp)
        Text("All Rights Reserved", color = TextSub, fontSize = 9.sp)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SettingsCard(iconRes: Int, title: String, subtitle: String, iconColor: Color = NeonCyan, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(SurfaceLight, SurfaceDark))),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = iconRes), contentDescription = title,
                    modifier = Modifier.size(22.dp), colorFilter = ColorFilter.tint(iconColor))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextSub, fontSize = 11.sp)
            }
            Text("›", color = NeonCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==================== DIALOG TENTANG ====================
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_info), contentDescription = null,
                    modifier = Modifier.size(28.dp), colorFilter = ColorFilter.tint(iconColor))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Tentang Aplikasi", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("IKR NETOPS", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Network Operations System", color = TextSub, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("Versi", "8.1")
                InfoRow("Rilis", "September 2026")
                InfoRow("Kategori", "Network Utility")
                InfoRow("Min Android", "7.0 (Nougat)")
                InfoRow("Target Android", "15 (API 35)")
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Deskripsi:", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Aplikasi manajemen router untuk YINET, Fiberhome, dan modem lainnya. Dilengkapi fitur auto-login, speed test akurat, dan konfigurasi manual.",
                    color = TextSub, fontSize = 11.sp, lineHeight = 16.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                Text("TUTUP", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSub, fontSize = 12.sp)
        Text(value, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ==================== DIALOG PENGEMBANG ====================
@Composable
fun DeveloperDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_developer), contentDescription = null,
                    modifier = Modifier.size(28.dp), colorFilter = ColorFilter.tint(iconColor))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Pengembang", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonCyan, Color(0xFF0099CC))))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.logo_ikr), contentDescription = "Foto Pengembang",
                        modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Themin Vigi Dwi Safik Reno", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Developer & Maintainer", color = TextSub, fontSize = 11.sp)
                Text("TEKNISI WIFI IKR NGADIROJO", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Terima kasih telah menggunakan aplikasi ini. Semoga bermanfaat untuk memudahkan pekerjaan teknisi WiFi di lapangan.",
                    color = TextSub, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                Text("TUTUP", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ==================== DIALOG DONASI ====================
@Composable
fun DonateDialog(context: Context, onDismiss: () -> Unit) {
    val danaNumber = "081358993202"
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_donate), contentDescription = null,
                    modifier = Modifier.size(28.dp), colorFilter = ColorFilter.tint(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Donasi", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))),
                    contentAlignment = Alignment.Center
                ) { Text("❤️", fontSize = 40.sp) }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Dukung Pengembangan", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Setiap donasi Anda sangat berarti untuk terus mengembangkan aplikasi ini.",
                    color = TextSub, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBg2),
                    border = BorderStroke(1.5.dp, EmeraldGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DANA", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(danaNumber, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("a/n Themin Vigi Dwi Safik Reno", color = TextSub, fontSize = 11.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("DANA", danaNumber))
                        Toast.makeText(context, "Nomor DANA disalin ✓", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📋  SALIN NOMOR DANA", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

// ==================== DIALOG MANUAL ====================
@Composable
fun ManualInputDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var ip by remember { mutableStateOf("http://192.168.1.1/html/login_inter.html") }
    var user by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.ic_router), contentDescription = null,
                    modifier = Modifier.size(26.dp), colorFilter = ColorFilter.tint(iconColor))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Konfigurasi Modem Lain", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Masukkan detail modem:", color = TextSub, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(value = ip, onValueChange = { ip = it },
                    label = { Text("Alamat IP / URL", color = TextSub) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = BorderColor,
                        focusedLabelColor = NeonCyan, cursorColor = NeonCyan,
                        focusedTextColor = TextMain, unfocusedTextColor = TextMain),
                    shape = RoundedCornerShape(10.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = user, onValueChange = { user = it },
                    label = { Text("Username", color = TextSub) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = BorderColor,
                        focusedLabelColor = NeonCyan, cursorColor = NeonCyan,
                        focusedTextColor = TextMain, unfocusedTextColor = TextMain),
                    shape = RoundedCornerShape(10.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = pw, onValueChange = { pw = it },
                    label = { Text("Password", color = TextSub) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = BorderColor,
                        focusedLabelColor = NeonCyan, cursorColor = NeonCyan,
                        focusedTextColor = TextMain, unfocusedTextColor = TextMain),
                    shape = RoundedCornerShape(10.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (ip.isNotBlank() && user.isNotBlank()) { onConfirm(ip, user, pw) }
            }, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                Text("BUKA ROUTER →", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSub)) {
                Text("BATAL")
            }
        }
    )
}

@Composable
fun UpdateDialog(info: VersionInfo, onDismiss: () -> Unit, onDownload: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = NeonCyan,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔔", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Pembaruan Tersedia", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Versi baru: " + info.versionName, color = EmeraldGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Changelog:", color = TextSub, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(info.changelog, color = TextMain, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Disarankan update untuk performa terbaik.", color = Amber, fontSize = 11.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload, colors = ButtonDefaults.textButtonColors(contentColor = EmeraldGreen)) {
                Text("DOWNLOAD", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!info.mandatory) {
                TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextSub)) {
                    Text("NANTI")
                }
            }
        }
    )
}
