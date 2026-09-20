package com.ikr.ngadirojo

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class RouterWebView : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetUrl = intent.getStringExtra("ROUTER_IP") ?: "http://192.168.1.1"
        val username = intent.getStringExtra("ROUTER_USER") ?: "admin"
        val password = intent.getStringExtra("ROUTER_PASS") ?: ""
        val autoLogin = intent.getBooleanExtra("AUTO_LOGIN", true)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
                    var webViewRef by remember { mutableStateOf<WebView?>(null) }
                    var showConfigPanel by remember { mutableStateOf(false) }
                    var showErrorDialog by remember { mutableStateOf(false) }
                    var errorMsg by remember { mutableStateOf("") }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36"
                                        overScrollMode = View.OVER_SCROLL_NEVER
                                        isScrollbarFadingEnabled = false
                                        isHapticFeedbackEnabled = false
                                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                        settings.setSupportZoom(true)
                                        settings.builtInZoomControls = true
                                        settings.displayZoomControls = false
                                        settings.textZoom = 100
                                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                                        setBackgroundColor(AndroidColor.WHITE)

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                                result?.confirm(); return true
                                            }
                                            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                                result?.confirm(); return true
                                            }
                                            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                                                result?.confirm(""); return true
                                            }
                                        }

                                        webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
            view?.postDelayed({ dumpACLHtml(view) }, 8000)
                                                view?.evaluateJavascript(
                                                    "javascript:(function() {" +
                                                    "var style = document.createElement('style');" +
                                                    "style.innerHTML = 'html,body{overscroll-behavior:none !important;overflow-x:hidden !important;} *{overscroll-behavior:contain !important;}';" +
                                                    "document.head.appendChild(style);" +
                                                    "})();",
                                                    null
                                                )
                                                if (autoLogin) {
                                                    view?.evaluateJavascript(buildAutoLoginJs(username, password), null)
                                                }
                                            }
                                        }
                                        loadUrl(targetUrl)
                                        webViewRef = this
                                    }
                                        
                                },
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )

                        }

                        // Floating Button Config
                        if (showErrorDialog) {
                            RouterErrorDialog(
                                message = errorMsg,
                                onRetry = { showErrorDialog = false; webViewRef?.reload() },
                                onDismiss = { showErrorDialog = false }
                            )
                        }
                        
                        FloatingActionButton(
                            onClick = { showConfigPanel = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 100.dp),
                            containerColor = NeonCyan,
                            contentColor = DarkBackground
                        ) {
                            Text("⚙️", fontSize = 24.sp)
                        }

                        // Panel Config
                        if (showConfigPanel) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xCC000000)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                RouterConfigPanel(
                                    webView = webViewRef,
                                    context = this@RouterWebView,
                                    onDismiss = { showConfigPanel = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== AUTO LOGIN SCRIPT ====================
fun buildAutoLoginJs(username: String, password: String): String {
    return "javascript:(function() {" +
        "function setVal(el, val) {" +
        "  try {" +
        "    var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
        "    setter.call(el, val);" +
        "  } catch(e) { el.value = val; }" +
        "  ['input','change','keyup','keydown','blur','focus'].forEach(function(evt){" +
        "    el.dispatchEvent(new Event(evt, {bubbles:true}));" +
        "  });" +
        "}" +
        "function attackDoc(win, doc) {" +
        "  if (!doc || !win) return false;" +
        "  try {" +
        "    var u = doc.getElementById('user_name') || doc.querySelector('input[name=\"user_name\"]');" +
        "    var p = doc.getElementById('loginpp') || doc.querySelector('input[name=\"loginpp\"]');" +
        "    if (!u || !p) {" +
        "      var inputs = doc.getElementsByTagName('input');" +
        "      var uf = null, pf = null;" +
        "      for (var i = 0; i < inputs.length; i++) {" +
        "        var t = (inputs[i].type || '').toLowerCase();" +
        "        var cls = inputs[i].className || '';" +
        "        if (t === 'password' && !pf) pf = inputs[i];" +
        "        else if ((t === 'text' || t === '') && !uf && cls.indexOf('security') === -1) uf = inputs[i];" +
        "      }" +
        "      if (!u) u = uf;" +
        "      if (!p) p = pf;" +
        "    }" +
        "    if (u && p) {" +
        "      setVal(u, '" + username + "');" +
        "      setVal(p, '" + password + "');" +
        "      if (typeof win.onlogin === 'function') { win.onlogin(1); return true; }" +
        "      var btn = doc.getElementById('login_btn') || doc.querySelector('input[type=\"submit\"]') || doc.querySelector('button');" +
        "      if (btn) { btn.click(); return true; }" +
        "      if (doc.forms.length > 0) { doc.forms[0].submit(); return true; }" +
        "    }" +
        "  } catch(e) {}" +
        "  return false;" +
        "}" +
        "function attackAll() {" +
        "  if (attackDoc(window, document)) return true;" +
        "  try {" +
        "    var frames = document.querySelectorAll('iframe, frame');" +
        "    for (var i=0; i<frames.length; i++) {" +
        "      try {" +
        "        var w = frames[i].contentWindow;" +
        "        var d = frames[i].contentDocument || w.document;" +
        "        if (attackDoc(w, d)) return true;" +
        "      } catch(e) {}" +
        "    }" +
        "  } catch(e) {}" +
        "  try {" +
        "    for (var k=0; k<window.frames.length; k++) {" +
        "      try {" +
        "        if (attackDoc(window.frames[k], window.frames[k].document)) return true;" +
        "      } catch(e) {}" +
        "    }" +
        "  } catch(e) {}" +
        "  return false;" +
        "}" +
        "var count = 0;" +
        "var interval = setInterval(function() {" +
        "  if (attackAll() || count > 50) { clearInterval(interval); }" +
        "  count++;" +
        "}, 400);" +
        "setInterval(function() {" +
        "  try {" +
        "    var c = document.getElementById('confirm');" +
        "    if (c && c.offsetParent !== null) { c.click(); }" +
        "  } catch(e) {}" +
        "}, 800);" +
        "window.alert = function() { return true; };" +
        "window.confirm = function() { return true; };" +
        "})();"
}

// ==================== NOTES BOTTOM BAR ====================
@Composable
fun NotesBottomBar() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var noteText by remember { mutableStateOf(NotesManager.load(context)) }
    var isEditing by remember { mutableStateOf(false) }
    var tempText by remember { mutableStateOf(noteText) }

    Surface(color = SurfaceDark, shadowElevation = 12.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(3.dp).height(14.dp).background(Amber))
                Spacer(modifier = Modifier.width(8.dp))
                Text("📝 CATATAN", color = Amber, fontSize = 10.sp, modifier = Modifier.weight(1f))
                if (!isEditing) {
                    TextButton(
                        onClick = { tempText = noteText; isEditing = true },
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                    ) {
                        Text("EDIT", fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    placeholder = { Text("Ketik catatan...", color = TextSub, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp, max = 100.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = NeonCyan,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { tempText = noteText; isEditing = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSub)
                    ) {
                        Text("BATAL", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            NotesManager.save(context, tempText)
                            noteText = tempText
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SIMPAN", fontSize = 11.sp)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (noteText.isBlank()) "Belum ada catatan. Tap EDIT untuk menulis." else noteText,
                        color = if (noteText.isBlank()) TextSub else TextMain,
                        fontSize = 11.sp,
                        maxLines = 3
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun RouterErrorDialog(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color(0xFFFFA500),
        textContentColor = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        title = {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text("⚠️", fontSize = 22.sp)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.material3.Text("Koneksi Gagal", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            androidx.compose.foundation.layout.Column {
                androidx.compose.material3.Text(message, color = Color(0xFFEF4444), fontSize = 12.sp)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                androidx.compose.material3.Text("Coba langkah berikut:", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                androidx.compose.material3.Text("1. Pastikan HP tersambung ke WiFi router", color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                androidx.compose.material3.Text("2. Cek kabel LAN/WAN sudah terpasang", color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                androidx.compose.material3.Text("3. Restart router (cabut & colok ulang)", color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                androidx.compose.material3.Text("4. Tekan COBA LAGI di bawah", color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onRetry, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color(0xFF10B981))) {
                androidx.compose.material3.Text("COBA LAGI", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                androidx.compose.material3.Text("TUTUP")
            }
        }
    )
}
