# Keep Kotlin metadata / serialization
-keepattributes *Annotation*, InnerClasses, Signature
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class com.anidesk.tv.**$$serializer { *; }
-keepclassmembers class com.anidesk.tv.** {
    *** Companion;
}
-keepclasseswithmembers class com.anidesk.tv.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor / OkHttp
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn org.slf4j.**

# Media3
-dontwarn androidx.media3.**