# -------------------------------------
# General Rules
# -------------------------------------

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn com.google.api.client.extensions.android.**
-dontwarn com.google.api.client.googleapis.extensions.android.**
-dontwarn com.google.android.gms.**

# -------------------------------------
# DataStore
# -------------------------------------

-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# -------------------------------------
# Parcelable
# -------------------------------------

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class com.twofasapp.feature.autofill.service.domain.AutofillLogin
-keep class com.twofasapp.feature.autofill.service.domain.SaveLoginData
-keep class com.twofasapp.feature.autofill.service.domain.SaveRequestSpec
-keep class com.twofasapp.feature.autofill.service.domain.FillRequestSpec

-keep class com.twofasapp.feature.autofill.service.parser.NodeStructure
-keep class com.twofasapp.feature.autofill.service.parser.MatchConfidence
-keep class com.twofasapp.feature.autofill.service.parser.AutofillInput
# -------------------------------------
# Kotlin Serialization
# -------------------------------------

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# -------------------------------------
# Crashlytics
# -------------------------------------

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# -------------------------------------
# Google Auth & API Client
# -------------------------------------

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}

-keep class com.google.api.services.drive.** { *; }

# -------------------------------------
# Credential Manager
# -------------------------------------

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
    *;
}

# -------------------------------------
# Navigation (App-Specific)
# -------------------------------------

-keepnames class com.twofasapp.core.android.navigation.*

-keep class com.twofasapp.core.android.navigation.Screen$** { *; }
-keep class com.twofasapp.core.android.navigation.ScreenType { *; }

-keepclassmembers enum com.twofasapp.core.android.navigation.Screen$** {
    <fields>;
    <methods>;
}

-keepclassmembers enum com.twofasapp.core.android.navigation.ScreenType {
    <fields>;
    <methods>;
}

# -------------------------------------
# PDFBox
# -------------------------------------

-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.gemalto.jp2.JP2Encoder

# -------------------------------------
# Enums & Serializable Support
# -------------------------------------

-keepclassmembers enum * { *; }
-keepclasseswithmembers enum * { *; }
