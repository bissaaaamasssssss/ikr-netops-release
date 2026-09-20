# Keep data classes
-keep class com.ikr.ngadirojo.** { *; }
-keepclassmembers class com.ikr.ngadirojo.** { *; }

# Keep JSON
-keep class org.json.** { *; }

# Keep Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }

# Keep Custom Tabs
-keep class androidx.browser.** { *; }
-keep class android.support.customtabs.** { *; }

# Keep WebView JS Interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Errorprone annotations (dari androidx.security:security-crypto)
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Tink (crypto)
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }

# General fallback
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
