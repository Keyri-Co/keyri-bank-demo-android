### Keyri Scanner
-dontshrink
-dontoptimize
-classobfuscationdictionary class-dictionary.txt
-packageobfuscationdictionary package-dictionary.txt

-keep class !com.keyrico.scanner.** { *; }

-keep public class com.keyrico.scanner.ScannerAuthActivity,
    com.keyrico.scanner.EasyKeyriAuth {
    public protected *;
    public static *;
}

-dontwarn java.lang.invoke.**

-keepattributes Signature, *Annotation*, Exceptions, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

### Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keepclassmembernames class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.coroutines.flow.**

### Kotlin
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

### Android Architecture Components
-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

# Lifecycle State and Event enums values
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}

### Material
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

### AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.* { *; }

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions.*
# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

### OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
