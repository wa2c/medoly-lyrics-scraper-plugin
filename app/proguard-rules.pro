# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\wa2c\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes *Annotation*

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep class com.google.**  { *; } # Gson, Google Data Java Client Library
-keep interface com.google.**  { *; } # Gson, Google Data Java Client Library


-keep class us.codecraft.xsoup.** { *; }
-keep interface us.codecraft.xsoup.** { *; }

-keep class org.jsoup.jsoup.** { *; }
-keep interface org.jsoup.jsoup.** { *; }

-keep class org.apache.httpcomponents.** { *; }
-keep interface org.apache.httpcomponents.** { *; }