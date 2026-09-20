package com.ikr.ngadirojo

import android.webkit.WebView
import java.io.File

fun dumpACLHtml(view: WebView?) {
    if (view == null) return
    val jsCode = """
        (function() {
            try {
                var iframe = document.querySelector('.main_iframe');
                var html = (iframe && iframe.contentDocument) ? iframe.contentDocument.documentElement.outerHTML : document.documentElement.outerHTML;
                return html;
            } catch(e) { return 'ERROR: ' + e.message; }
        })();
    """.trimIndent()
    view.evaluateJavascript(jsCode) { result ->
        try {
            val clean = result?.replace("\\u003C", "<")?.replace("\\u003E", ">")
                ?.replace("\\\"", "\"")?.replace("\\n", "\n")?.replace("\\/", "/")
                ?.trim('"') ?: ""
            if (clean.contains("acl_enable", ignoreCase = true)) {
                File("/sdcard/Download/acl_html.txt").writeText(clean)
            }
        } catch(e: Exception) { }
    }
}

fun navigateToACL(view: WebView?) {
    if (view == null) return
    val jsCode = """
        (function() {
            function clickByText(text) {
                var els = document.querySelectorAll('li, span, a');
                for (var i = 0; i < els.length; i++) {
                    var t = (els[i].innerText || '').trim().toUpperCase();
                    if (t === text.toUpperCase() && els[i].offsetParent !== null) { els[i].click(); return true; }
                }
                return false;
            }
            clickByText('SECURITY');
            setTimeout(function() {
                clickByText('FIREWALL');
                setTimeout(function() { clickByText('ACL SETTINGS'); }, 1500);
            }, 1000);
        })();
    """.trimIndent()
    view.evaluateJavascript(jsCode, null)
}


fun runAutoACL(view: WebView?, onStatus: (String) -> Unit) {
    if (view == null) { onStatus("❌ WebView null"); return }
    onStatus("⏳ Membuka ACL Settings...")
    view.evaluateJavascript(buildAutoNavJsACL(), null)
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        onStatus("⏳ Mengisi form ACL...")
        val rules = listOf(ACLRule(1))
        view.evaluateJavascript(buildACLFillJs(true, rules)) { result ->
            val clean = result?.replace("\"", "") ?: ""
            if (clean.contains("SUCCESS")) onStatus("✅ Alhamdulillah wes mas")
            else onStatus("⚠️ Sebagian. Coba lagi.")
        }
    }, 6000)
}

