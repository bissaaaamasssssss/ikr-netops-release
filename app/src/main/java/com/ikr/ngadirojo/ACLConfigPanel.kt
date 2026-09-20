package com.ikr.ngadirojo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ACLRule(
    val id: Int,
    val active: String = "YES",
    val srcIp: String = "",
    val protocol: String = "ALL",
    val interfaceName: String = "WAN"
)

@Composable
fun ACLConfigPanel(
    webView: WebView?,
    context: Context,
    onDismiss: () -> Unit
) {
    var aclEnable by remember { mutableStateOf(true) }
    var rules by remember { mutableStateOf(listOf(ACLRule(1))) }
    var statusMsg by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    fun applyACL() {
        if (webView == null) {
            statusMsg = "❌ WebView belum siap"
            return
        }
        isProcessing = true
        statusMsg = "⏳ BERJALAN MAS SABAR!"

        val navJs = buildAutoNavJsACL()
        webView.evaluateJavascript(navJs, null)

        Handler(Looper.getMainLooper()).postDelayed({
            val fillJs = buildACLFillJs(aclEnable, rules)
            webView.evaluateJavascript(fillJs) { result ->
                isProcessing = false
                val clean = result?.replace("\"", "") ?: ""
                if (clean.contains("SUCCESS")) {
                    statusMsg = "✅ Alhamdulillah wes mas"
                } else if (clean.contains("PARTIAL")) {
                    statusMsg = "⚠️ Sebagian berhasil. Coba APPLY lagi."
                } else {
                    statusMsg = "❌ Gagal. Buka halaman Security → Firewall → ACL Settings dulu."
                }
            }
        }, 6000)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(2.dp, Color(0xFFF59E0B)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp).heightIn(max = 650.dp).verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛡️", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "SETTING ACL (FIREWALL)",
                    color = Amber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDismiss) {
                    Text("✕", color = TextSub, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Status
            if (statusMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
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
                    Text(
                        statusMsg,
                        color = when {
                            statusMsg.contains("✅") -> EmeraldGreen
                            statusMsg.contains("❌") -> Color(0xFFEF4444)
                            else -> Amber
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACL Enable
            Text("ACL Enable:", color = TextSub, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = aclEnable,
                    onClick = { aclEnable = true },
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                )
                Text("Enable", color = if (aclEnable) EmeraldGreen else TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(20.dp))
                RadioButton(
                    selected = !aclEnable,
                    onClick = { aclEnable = false },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEF4444))
                )
                Text("Disable", color = if (!aclEnable) Color(0xFFEF4444) else TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rules List
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rules List:", color = TextSub, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    rules = rules + ACLRule(rules.size + 1)
                }) {
                    Text("+ TAMBAH RULE", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            rules.forEachIndexed { index, rule ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBg2),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rule #${rule.id}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (rules.size > 1) {
                                TextButton(onClick = {
                                    rules = rules.filter { it.id != rule.id }
                                }) {
                                    Text("🗑️", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }
                        }

                        // Active
                        MiniDropdown(
                            label = "Active",
                            value = rule.active,
                            options = listOf("YES", "NO"),
                            onSelect = { newVal ->
                                rules = rules.map { if (it.id == rule.id) it.copy(active = newVal) else it }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Src IP
                        OutlinedTextField(
                            value = rule.srcIp,
                            onValueChange = { v ->
                                rules = rules.map { if (it.id == rule.id) it.copy(srcIp = v) else it }
                            },
                            label = { Text("Src IP (kosong = semua)", color = TextSub, fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Amber,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Protocol
                        MiniDropdown(
                            label = "Protocol",
                            value = rule.protocol,
                            options = listOf("ALL", "TCP", "UDP", "ICMP"),
                            onSelect = { v ->
                                rules = rules.map { if (it.id == rule.id) it.copy(protocol = v) else it }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Interface
                        MiniDropdown(
                            label = "Interface",
                            value = rule.interfaceName,
                            options = listOf("WAN", "LAN", "WLAN"),
                            onSelect = { v ->
                                rules = rules.map { if (it.id == rule.id) it.copy(interfaceName = v) else it }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tombol Apply
            Button(
                onClick = { applyACL() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = DarkBackground,
                    disabledContainerColor = Color(0xAA334155),
                    disabledContentColor = TextSub
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isProcessing) "⏳ BERJALAN MAS SABAR!" else "⚡ APPLY ACL OTOMATIS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x22F59E0B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "💡 CARA PAKAI:\n" +
                    "1. Buka manual: Security → Firewall → ACL Settings\n" +
                    "2. Tap tombol ⚙️ → tap SETTING ACL\n" +
                    "3. Atur Enable + Rules\n" +
                    "4. Tap APPLY ACL OTOMATIS",
                    color = Amber,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun MiniDropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSub, fontSize = 10.sp, modifier = Modifier.width(70.dp))
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain)
            ) {
                Text(value, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("▼", color = Amber, fontSize = 9.sp)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, color = if (opt == value) Amber else TextMain, fontSize = 11.sp) },
                        onClick = { onSelect(opt); expanded = false }
                    )
                }
            }
        }
    }
}

// ============ AUTO-NAVIGATE ACL ============
fun buildAutoNavJsACL(): String {
    return """
        javascript:(function() {
            function clickByText(txt) {
                var els = document.querySelectorAll('li, span, a');
                for (var i = 0; i < els.length; i++) {
                    var e = els[i];
                    var t = (e.innerText || e.textContent || '').trim().toUpperCase();
                    if (t === txt.toUpperCase()) {
                        try { e.click(); } catch(x) {}
                        try { e.dispatchEvent(new MouseEvent('click', {bubbles:true})); } catch(x) {}
                        return true;
                    }
                }
                return false;
            }
            clickByText('SECURITY');
            setTimeout(function() {
                clickByText('FIREWALL');
                setTimeout(function() {
                    clickByText('ACL SETTINGS');
                }, 1800);
            }, 1500);
            return 'NAV-STARTED';
        })();
    """.trimIndent()
}

// ============ FILL ACL FORM ============
fun buildACLFillJs(enable: Boolean, rules: List<ACLRule>): String {
    val radioId = if (enable) "#acl_enable" else "#acl_disable"
    return """
        javascript:(function() {
            var log = [];
            function getDoc() {
                var iframe = document.querySelector('.main_iframe');
                if (iframe && iframe.contentDocument) return iframe.contentDocument;
                return document;
            }
            var doc1 = getDoc();
            var radio = doc1.querySelector('$radioId');
            if (radio) { radio.click(); log.push('radio'); }
            var applyBtn = doc1.querySelector('#acl_enable_apply');
            if (applyBtn) { applyBtn.click(); log.push('applyEnable'); }

            setTimeout(function() {
                var doc2 = getDoc();
                var addBtn = doc2.querySelector('#fw_add');
                if (addBtn) { addBtn.click(); log.push('add'); }

                setTimeout(function() {
                    var doc3 = getDoc();
                    var act = doc3.querySelector('#acl_active');
                    if (act) { act.value = '1'; }
                    var dir = doc3.querySelector('#acl_direction');
                    if (dir) { dir.value = '1'; }
                    var proto = doc3.querySelector('#acl_protocol');
                    if (proto) { proto.value = 'ALL'; }
                    var save = doc3.querySelector('#firewall_apply');
                    if (save) { save.click(); }
                }, 3500);
            }, 3500);

            return 'SUCCESS|' + log.join(',');
        })();
    """.trimIndent()
}
