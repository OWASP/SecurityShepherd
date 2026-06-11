# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ===================================
# REVERSE ENGINEERING LESSONS/CHALLENGES
# Keep these unobfuscated for educational purposes
# ===================================

# Reverse Engineering Lesson - meant to be reverse engineered
-keep class org.owasp.mobileshepherd.ui.lessons.** { *; }

# Reverse Engineering Challenges 1-3 - meant to be reverse engineered
-keep class org.owasp.mobileshepherd.ui.challenges.** { *; }

# ===================================
# OBFUSCATE EVERYTHING ELSE
# These should be protected from reverse engineering
# ===================================

# Keep names for ViewBinding and ViewModels to avoid runtime issues
-keep class org.owasp.mobileshepherd.databinding.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep MainActivity and navigation infrastructure
-keep class org.owasp.mobileshepherd.MainActivity { *; }
-keep class org.owasp.mobileshepherd.LandingActivity { *; }
-keep class org.owasp.mobileshepherd.Preferences { *; }

# Keep Fragment classes but obfuscate their internals
-keepnames class * extends androidx.fragment.app.Fragment

# Keep these challenge/lesson packages OBFUSCATED (not in RE category)
# Insecure Data Storage - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.insecuredata.** 
-keepnames class org.owasp.mobileshepherd.ui.challenges.insecuredata1.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.insecuredata3.**

# Poor Authentication - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.poorauth.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.poorauth.**

# Supply Chain Security - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.supplychain.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.supplychain.**

# Insecure Communication - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.insecurecomm.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.insecurecomm.**

# Cryptography - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.crypto.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.crypto.**

# Security Misconfiguration - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.securitymisconfig.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.securitymisconfig.**

# Input Validation - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.inputvalidation.**
-keepnames class org.owasp.mobileshepherd.ui.challenges.inputvalidation.**

# Insecure Authorization - should be obfuscated
-keepnames class org.owasp.mobileshepherd.ui.lessons.insecureauthorization.**

# Home fragment
-keepnames class org.owasp.mobileshepherd.ui.home.**

# Aggressive obfuscation settings
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification

# Obfuscate string constants (except in RE packages)
-adaptclassstrings

# Remove logging for non-RE packages
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep source file and line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AndroidX and Material Components
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Navigation component
-keep class androidx.navigation.** { *; }

# Prevent stripping of enum classes
-keepclassmembers enum * { *; }