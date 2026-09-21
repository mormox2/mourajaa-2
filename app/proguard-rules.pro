# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers for stack traces and crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database keep rules
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.example.data.local.AppDatabase { *; }
-dontwarn androidx.room.paging.**

# Domain Models
-keep class com.example.domain.model.** { *; }

# Coil image loading
-keep class coil.** { *; }
