# ==================== 游戏私服工具箱 ProGuard 规则 ====================

# ==================== Kotlin ====================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# ==================== Kotlinx Serialization ====================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.gameserver.toolbox.**$$serializer { *; }
-keepclassmembers class com.gameserver.toolbox.** { *** Companion; }
-keepclasseswithmembers class com.gameserver.toolbox.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==================== 数据模型（JSON 序列化需要字段名保留） ====================
-keepclassmembers class com.gameserver.toolbox.data.model.** {
    <fields>;
    <init>(...);
}

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.logging.**

# ==================== AndroidX Security Crypto ====================
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.api.client.**
-dontwarn javax.annotation.**
-dontwarn org.joda.time.**
-dontwarn org.checkerframework.**

# ==================== 反射保留 ====================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ==================== 行号保留（混淆方法名但保留堆栈可读） ====================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ==================== 移除日志 ====================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# ==================== 枚举 ====================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== Activity/Service（AndroidManifest 反射，只保留类名） ====================
-keepnames class com.gameserver.toolbox.MainActivity
-keepnames class com.gameserver.toolbox.service.SystemFloatingWindowService

# ==================== API 常量（被 OkHttp 直接引用） ====================
-keep class com.gameserver.toolbox.core.constants.** { *; }

# ==================== API 接口层（被反射/动态代理） ====================
-keep class com.gameserver.toolbox.data.service.api.** { *; }