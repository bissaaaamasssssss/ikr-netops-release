package com.ikr.ngadirojo

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SatriaScreen() {
    val context = LocalContext.current
    val satriaUrl = "https://sites.google.com/view/satria2000/satria-2000"
    var hasOpened by remember { mutableStateOf(false) }

    // Auto-open Custom Tab saat screen pertama kali muncul
    LaunchedEffect(Unit) {
        if (!hasOpened) {
            hasOpened = true
            openCustomTab(context, satriaUrl)
        }
    }

    // Tampilan loading screen saat Custom Tab buka
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkBg2))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Brush.linearGradient(listOf(NeonCyan, Color(0xFF0099CC))), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🛰️", fontSize = 44.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "SATRIA 2000",
            color = NeonCyan,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Portal Teknisi & Tools",
            color = TextSub,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        CircularProgressIndicator(color = NeonCyan, strokeWidth = 3.dp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Membuka SATRIA 2000...",
            color = TextSub,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Tombol untuk buka ulang kalau Custom Tab sudah tertutup
        TextButton(
            onClick = { openCustomTab(context, satriaUrl) },
            colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
        ) {
            Text("🔄  BUKA ULANG", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Tap X di toolbar untuk kembali ke aplikasi",
            color = TextSub,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

fun openCustomTab(context: android.content.Context, url: String) {
    try {
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(Color(0xFF0A0F1E).toArgb())
        builder.setSecondaryToolbarColor(Color(0xFF00E5FF).toArgb())
        builder.setNavigationBarColor(Color(0xFF0A0F1E).toArgb())
        builder.setShowTitle(true)
        builder.setInstantAppsEnabled(false)
        builder.setUrlBarHidingEnabled(false)

        val customTabsIntent = builder.build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.android.chrome")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e2: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(Intent.createChooser(intent, "Buka dengan"))
        }
    }
}
