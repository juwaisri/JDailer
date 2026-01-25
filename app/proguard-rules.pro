# Project-specific ProGuard rules
-dontwarn kotlinx.coroutines.**
-dontwarn org.jetbrains.annotations.**
-keep class androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
