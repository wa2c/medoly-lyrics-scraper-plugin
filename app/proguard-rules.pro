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
-keep class com.github.gfx.android.orma.** { *; }
-dontwarn com.github.gfx.android.orma.**

-keep class org.assertj.core.** {*;}
-dontwarn org.assertj.core.**

-keep class org.jsoup.** {*;}
-dontwarn org.jsoup.**

-keep class us.codecraft.** {*;}
-dontwarn us.codecraft.**

-dontnote org.apache.http.**
-dontnote android.net.http.**
