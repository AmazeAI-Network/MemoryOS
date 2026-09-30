# ==============================================================================
# Enterprise R8 & ProGuard Production Rules for MemoryOS Independent Distribution
# Obfuscation, Minification, Reflection Protection & Anti-Reverse Engineering
# ==============================================================================

# 1. Aggressive Obfuscation & Bytecode Optimization
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses ''

# Preserve line numbers for symbolication in crash logs, while masking actual file paths
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,Deprecated,*Annotation*

# Strip developer log statements in release builds to prevent information leakage
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(java.lang.String, java.lang.String);
}

# 2. Room Database Rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Fts4 class * { *; }
-dontwarn androidx.room.paging.**

# 3. Moshi & Kotlin Serialization / Reflection Protection
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.JsonClass <fields>;
}
-keep class com.squareup.moshi.** { *; }
-keep class *JsonAdapter {
    public <init>(...);
    public ** fromJson(...);
    public void toJson(...);
}
-dontwarn com.squareup.moshi.**

# 4. Networking: OkHttp & Retrofit
-keepattributes EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# 5. Coil Image Loading
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# 6. AndroidX Security Crypto & MasterKey Keystore Access
-keep class androidx.security.crypto.** { *; }
-keep interface androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# 7. JNI Native Code Protection (NDK Secure Key Storage)
-keepclasseswithmembernames class * {
    native <methods>;
}

# 8. RevenueCat In-App Purchases
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# 9. Jetpack Compose & Kotlin Coroutines
-keep class androidx.compose.runtime.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 10. FileProvider and App Update Components
-keep class androidx.core.content.FileProvider { *; }
-keep class com.example.updater.AppUpdateInfo { *; }
-keep class com.example.crash.** { *; }

# 11. Compose Compiler Stability & Bytecode Stripping for 60/120 FPS
-keep @androidx.compose.runtime.Immutable class * { *; }
-keep @androidx.compose.runtime.Stable class * { *; }
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    void sourceInformation(...);
    void sourceInformationMarkerStart(...);
    void sourceInformationMarkerEnd(...);
}
