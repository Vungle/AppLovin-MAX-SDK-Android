buildscript {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://maven.fabric.io/public") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { setUrl("https://repository.jetbrains.com/all") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("com.android.library:com.android.library.gradle.plugin:7.3.0")
        classpath(kotlin("gradle-plugin", version = "1.7.10"))
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

plugins {
    id("signing")
    id("maven-publish")
    id("com.android.library")
}
apply(plugin = "com.android.library")

android {
    defaultConfig {
        compileSdk = 31
        minSdk = 16
        buildConfigField("int", "VERSION_CODE", "07000000")
        buildConfigField("String", "VERSION_NAME", "\"7.0.0.0\"");
    }
}


private val versionMajor = 7
private val versionMinor = 0
private val versionPatch = 0
private val versionAdapterPatch = 0

val libraryVersionName by extra("${versionMajor}.${versionMinor}.${versionPatch}.${versionAdapterPatch}")
val libraryVersionCode by extra((versionMajor * 1000000) + (versionMinor * 10000) + (versionPatch * 100) + versionAdapterPatch)

val libraryArtifactId by extra("vungle-adapter")

dependencies {
//    implementation("com.vungle:publisher-sdk-android:6.12.0")
    implementation("com.applovin:applovin-sdk:+@aar")
    implementation(project(":vng-android-sdk:vungle-ads"))
}
