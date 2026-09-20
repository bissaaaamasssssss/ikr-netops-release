package com.ikr.ngadirojo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BandConfig(
    val enabled: Boolean = false,
    val namaWifi: String = "",
    val password: String = "",
    val securityMode: String = "WPA2-PSK",
    val enkripsi: String = "AES"
)

val SECURITY_OPTIONS = listOf(
    "OpenSystem",
    "WPA2-PSK",
    "WPA-PSK/WPA2-PSK",
    "WPA2-PSK/WPA3-SAE",
    "WPA3-SAE"
)

@Composable
fun RouterConfigPanel(
    webView: WebView?,
    context: Context,
    onDismiss: () -> Unit
) {
    var statusMsg by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var config4G by remember { mutableStateOf(BandConfig()) }
    var config5G by remember { mutableStateOf(BandConfig(enabled = true)) }
    var showNotes by remember { mutableStateOf(false) }
    var showACL by remember { mutableStateOf(false) }

    // Dialog Catatan
    if (showNotes) {
        NotesDialog(context = context, onDismiss = { showNotes = false })
    }

    if (showACL) {
        ACLConfigPanel(
            webView = webView,
            context = context,
            onDismiss = { showACL = false }
        )
    }

    fun applyBand(band: String, config: BandConfig, onDone: () -> Unit = {}) {
        if (webView == null) {
            statusMsg = "❌ WebView belum siap"
            onDone()
            return
        }
        if (!config.enabled) { onDone(); return }
        if (config.namaWifi.isBlank()) {
            statusMsg = "❌ Nama WiFi $band wajib diisi"
            onDone()
            return
        }
        if (config.password.length < 8) {
            statusMsg = "❌ Password $band minimal 8 karakter"
            onDone()
            return
        }

        statusMsg = "⏳ BERJALAN MAS SABAR!"

        val navJs = buildAutoNavJs(band)
        webView.evaluateJavascript(navJs, null)

        Handler(Looper.getMainLooper()).postDelayed({
            val fillJs = buildWifiFillJs(
                config.namaWifi,
                config.password,
                config.securityMode,
                config.enkripsi,
                band
            )
            webView.evaluateJavascript(fillJs) { result ->
                val clean = result?.replace("\"", "") ?: ""
                if (clean.contains("SUCCESS")) {
                    statusMsg = "✅ Alhamdulillah wes mas"
                } else if (clean.contains("PARTIAL")) {
                    statusMsg = "⚠️ $band: form tidak ketemu. Coba APPLY lagi."
                } else {
                    statusMsg = "❌ $band gagal."
                }
                onDone()
            }
        }, 6000)
    }

    if (!showACL) {
        Card(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(2.dp, NeonCyan),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp).heightIn(max = 650.dp).verticalScroll(rememberScrollState())
        ) {
            // Header — Icon WiFi profesional + judul
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon WiFi profesional dengan background circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonCyan, EmeraldGreen)),
                            CircleShape
                        )
                        .border(1.5.dp, Color(0x9900E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wifi_pro),
                        contentDescription = "WiFi",
                        modifier = Modifier.size(26.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "SETTING WIFI",
                    color = NeonCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )
                    Surface(
            color = Color(0x33F59E0B),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable {
                if (webView != null) {
                    android.widget.Toast.makeText(context, "Auto ACL berjalan, tunggu...", android.widget.Toast.LENGTH_SHORT).show()
                    runAutoACL(webView) { msg ->
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    android.widget.Toast.makeText(context, "WebView belum siap", android.widget.Toast.LENGTH_SHORT).show()
                }
                onDismiss()
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛡️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("AUTO ACL", fontSize = 10.sp, color = Amber, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
                TextButton(onClick = onDismiss) {
                    Text("✕", color = TextSub, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }


                        // ===== STATUS MESSAGE DI BAWAH JUDUL =====
            if (statusMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            statusMsg.contains("✅") -> Color(0x3310B981)
                            statusMsg.contains("❌") -> Color(0x33EF4444)
                            else -> Color(0x33F59E0B)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        when {
                            statusMsg.contains("✅") -> EmeraldGreen
                            statusMsg.contains("❌") -> Color(0xFFEF4444)
                            else -> Amber
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when {
                                statusMsg.contains("✅") -> "✅"
                                statusMsg.contains("❌") -> "❌"
                                else -> "⏳"
                            },
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            statusMsg,
                            color = when {
                                statusMsg.contains("✅") -> EmeraldGreen
                                statusMsg.contains("❌") -> Color(0xFFEF4444)
                                else -> Amber
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BandColumn(
                    title = "4G Advanced",
                    config = config4G,
                    onConfigChange = { config4G = it },
                    modifier = Modifier.weight(1f),
                    onApply = { applyBand("4G", config4G) }
                )
                BandColumn(
                    title = "5G Advanced",
                    config = config5G,
                    onConfigChange = { config5G = it },
                    modifier = Modifier.weight(1f),
                    onApply = { applyBand("5G", config5G) }
                )
            }

            if (config4G.enabled && config5G.enabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (isProcessing) return@Button
                        isProcessing = true
                        applyBand("4G", config4G) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                applyBand("5G", config5G) {
                                    isProcessing = false
                                }
                            }, 3000)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = DarkBackground,
                        disabledContainerColor = Color(0xAA334155),
                        disabledContentColor = TextSub
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isProcessing) "⏳ BERJALAN MAS SABAR!" else "⚡ APPLY 4G & 5G BARENG",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

        }
    }
    }
}

@Composable
fun BandColumn(
    title: String,
    config: BandConfig,
    onConfigChange: (BandConfig) -> Unit,
    modifier: Modifier = Modifier,
    onApply: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (config.enabled) Color(0x1A00E5FF) else Color(0x331B2942)
        ),
        border = BorderStroke(
            width = if (config.enabled) 2.dp else 1.dp,
            color = if (config.enabled) NeonCyan else BorderColor
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = config.enabled,
                    onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeonCyan,
                        uncheckedColor = TextSub,
                        checkmarkColor = DarkBackground
                    )
                )
                Text(
                    title,
                    color = if (config.enabled) NeonCyan else TextMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Nama WiFi
            OutlinedTextField(
                value = config.namaWifi,
                onValueChange = { onConfigChange(config.copy(namaWifi = it)) },
                label = { Text("Nama WiFi", color = TextSub, fontSize = 10.sp) },
                placeholder = { Text("IKR_NGADIROJO", color = TextSub, fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = config.enabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderColor,
                    disabledBorderColor = BorderColor,
                    focusedLabelColor = NeonCyan,
                    cursorColor = NeonCyan,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain,
                    disabledTextColor = TextSub
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Password + ICON MATA
            OutlinedTextField(
                value = config.password,
                onValueChange = { onConfigChange(config.copy(password = it)) },
                label = { Text("Password", color = TextSub, fontSize = 10.sp) },
                placeholder = { Text("min 8", color = TextSub, fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = config.enabled,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            if (passwordVisible) "🙈" else "👁",
                            fontSize = 16.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderColor,
                    disabledBorderColor = BorderColor,
                    focusedLabelColor = NeonCyan,
                    cursorColor = NeonCyan,
                    focusedTextColor = TextMain,
                    unfocusedTextColor = TextMain,
                    disabledTextColor = TextSub
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Security Mode (RADIO - pilih 1)
            Text("Security Mode:", color = TextSub, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                SECURITY_OPTIONS.forEach { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = config.enabled) {
                                onConfigChange(config.copy(securityMode = opt))
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.securityMode == opt,
                            onClick = { if (config.enabled) onConfigChange(config.copy(securityMode = opt)) },
                            enabled = config.enabled,
                            modifier = Modifier.size(22.dp),
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NeonCyan,
                                unselectedColor = TextSub,
                                disabledSelectedColor = Color(0x5500E5FF),
                                disabledUnselectedColor = Color(0x55334155)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            opt,
                            color = if (config.enabled && config.securityMode == opt) NeonCyan else TextSub,
                            fontSize = 10.sp,
                            fontWeight = if (config.securityMode == opt) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enkripsi
            Text("Enkripsi:", color = TextSub, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                MiniChip("AES", config.enkripsi, config.enabled) { onConfigChange(config.copy(enkripsi = "AES")) }
                Spacer(modifier = Modifier.width(4.dp))
                MiniChip("TKIP", config.enkripsi, config.enabled) { onConfigChange(config.copy(enkripsi = "TKIPAES")) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                enabled = config.enabled && config.namaWifi.isNotBlank() && config.password.length >= 8,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkBackground,
                    disabledContainerColor = Color(0xAA334155),
                    disabledContentColor = TextSub
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⚡ APPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MiniChip(label: String, selected: String, enabled: Boolean, onClick: () -> Unit) {
    val isSelected = label == selected || (label == "TKIP" && selected == "TKIPAES")
    Surface(
        modifier = Modifier.height(24.dp).clickable(enabled = enabled) { onClick() },
        color = if (isSelected && enabled) NeonCyan else SurfaceLight,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (isSelected && enabled) NeonCyan else BorderColor)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (isSelected && enabled) DarkBackground else TextSub,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============ NOTES DIALOG (dipindah ke sini) ============
@Composable
fun NotesDialog(context: Context, onDismiss: () -> Unit) {
    var noteText by remember { mutableStateOf(NotesManager.load(context)) }
    var tempText by remember { mutableStateOf(noteText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = Amber,
        textContentColor = TextMain,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📝", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("CATATAN TEKNISI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    placeholder = { Text("Ketik catatan...", color = TextSub, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 350.dp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = NeonCyan,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { tempText = "" },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("HAPUS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            NotesManager.save(context, tempText)
                            noteText = tempText
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SIMPAN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

// ============ AUTO-NAVIGATE ============
fun buildAutoNavJs(targetBand: String): String {
    val searchTerm = if (targetBand == "4G") "2.4G ADVANCED" else "5G ADVANCED"
    return """
        javascript:(function() {
            var log = [];
            function clickByText(keyword) {
                var key = keyword.toUpperCase();
                var all = document.querySelectorAll('a, li, span, div, button');
                // Prioritas 1: exact match
                for (var i = 0; i < all.length; i++) {
                    var e = all[i];
                    if (!e.offsetParent) continue;
                    var t = (e.innerText || e.textContent || '').trim().toUpperCase();
                    if (t === key) { 
                        try { e.click(); } catch(x){}
                        try { e.dispatchEvent(new MouseEvent('click', {bubbles:true})); } catch(x){}
                        log.push('OK:' + key);
                        return true; 
                    }
                }
                // Prioritas 2: partial match
                for (var j = 0; j < all.length; j++) {
                    var e2 = all[j];
                    if (!e2.offsetParent) continue;
                    var t2 = (e2.innerText || e2.textContent || '').trim().toUpperCase();
                    if (t2.indexOf(key) !== -1 && t2.length < 40) {
                        try { e2.click(); } catch(x){}
                        try { e2.dispatchEvent(new MouseEvent('click', {bubbles:true})); } catch(x){}
                        log.push('PARTIAL:' + key);
                        return true;
                    }
                }
                log.push('FAIL:' + key);
                return false;
            }

            // Step 1: klik NETWORK
            clickByText('NETWORK');
            setTimeout(function() {
                // Step 2: klik WLAN SETTINGS (atau WLAN)
                if (!clickByText('WLAN SETTINGS')) clickByText('WLAN');
                setTimeout(function() {
                    // Step 3: klik band target
                    clickByText('$searchTerm');
                    console.log('AutoNav log: ' + log.join(', '));
                }, 2000);
            }, 1800);
        })();
    """.trimIndent()
}

// ============ FILL FORM ============
fun buildWifiFillJs(namaWifi: String, passwordWifi: String, secMode: String, wpaAlgorithm: String, targetBand: String): String {
    return """
        javascript:(function() {
            var log = [];
            function setV(el, val) {
                if (!el) return false;
                try {
                    var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                    setter.call(el, val);
                } catch(e) { el.value = val; }
                ['input','change','keyup','keydown','blur','focus'].forEach(function(evt){
                    el.dispatchEvent(new Event(evt, {bubbles:true}));
                });
                return true;
            }
            function setSelect(el, val) {
                if (!el) return false;
                var target = val.toUpperCase();
                for (var i = 0; i < el.options.length; i++) {
                    var t = (el.options[i].text || '').toUpperCase().trim();
                    var v = (el.options[i].value || '').toUpperCase().trim();
                    if (t === target || v === target || t.indexOf(target) !== -1 || v.indexOf(target) !== -1) {
                        el.selectedIndex = i;
                        el.dispatchEvent(new Event('change', {bubbles:true}));
                        el.dispatchEvent(new Event('input', {bubbles:true}));
                        return true;
                    }
                }
                return false;
            }
            function getDocs() {
                var docs = [document];
                try {
                    var frames = document.querySelectorAll('iframe, frame');
                    for (var i = 0; i < frames.length; i++) {
                        try {
                            var d = frames[i].contentDocument || frames[i].contentWindow.document;
                            if (d && d.body) docs.push(d);
                        } catch(e) {}
                    }
                } catch(e) {}
                return docs;
            }
            function fillDoc(doc) {
                if (!doc || !doc.body) return false;
                var ssid = doc.querySelector("input[name='ssid']") ||
                           doc.querySelector("input[id='ssid']") ||
                           doc.querySelector("input[name='SSID']") ||
                           doc.querySelector("input[name='wifi_ssid']") ||
                           doc.querySelector("input[name='essid']");
                var pwd = doc.querySelector("input[name='passphrase']") ||
                          doc.querySelector("input[name='Passphrase']") ||
                          doc.querySelector("input[name='wpapsk']") ||
                          doc.querySelector("input[name='wpa_passphrase']") ||
                          doc.querySelector("input[name='password']") ||
                          doc.querySelector("input[type='password']");
                if (!ssid && !pwd) return false;
                if (ssid) { setV(ssid, '$namaWifi'); log.push('SSID'); }
                if (pwd) { setV(pwd, '$passwordWifi'); log.push('PWD'); }
                var secSel = doc.querySelector("select[name*='ecurity']") ||
                             doc.querySelector("select[id*='ecurity']") ||
                             doc.querySelector("select[name='security_mode']");
                if (secSel) {
                    if (setSelect(secSel, '$secMode')) log.push('SEC');
                }
                setTimeout(function() {
                    var btns = doc.querySelectorAll("button, input[type='submit'], input[type='button'], a");
                    for (var i = 0; i < btns.length; i++) {
                        var t = (btns[i].innerText || btns[i].value || '').toLowerCase().trim();
                        if (t === 'apply' || t === 'save' || t === 'simpan' || t === 'terapkan') {
                            btns[i].click();
                            log.push('APPLY');
                            return;
                        }
                    }
                }, 1500);
                return true;
            }
            var docs = getDocs();
            for (var i = 0; i < docs.length; i++) {
                if (fillDoc(docs[i])) return 'SUCCESS|' + log.join(',');
            }
            return 'PARTIAL|Form tidak ketemu';
        })();
    """.trimIndent()
}
