# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes EnclosingMethod
-keepattributes InnerClasses

-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
-keep class com.revenuecat.purchases.** { *; }

-keep class com.intuisoft.plaid.common.local.UserData**
-keepclassmembers class com.intuisoft.plaid.common.local.UserData** {*;}

-keep class kotlin.** {*;}
-keep class java.util.** {*;}

-keep class com.intuisoft.plaid.common.model.** {*;}

-keep class io.horizontalsystems.** {*;}

-keep class io.horizontalsystems.com.intuisoft.plaid.walletmanager.WalletIdentifier** {*;}

-keep public class com.google.gson.**