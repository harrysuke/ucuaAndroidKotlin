# Retrofit + kotlinx.serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,includedescriptorclasses class com.ucua.app.**$$serializer { *; }
-keepclasseswithmembers class com.ucua.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
