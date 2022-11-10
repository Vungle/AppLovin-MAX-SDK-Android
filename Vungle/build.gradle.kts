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
        classpath("com.android.library:com.android.library.gradle.plugin:7.2.2")
        classpath(kotlin("gradle-plugin", version = "1.3.70"))
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
        compileSdkVersion(31)
        minSdkVersion(16)
        buildConfigField("int", "VERSION_CODE", "06120100")
        buildConfigField("String", "VERSION_NAME", "\"6.12.1.0\"");
    }
}


private val versionMajor = 6
private val versionMinor = 12
private val versionPatch = 1
private val versionAdapterPatch = 0

val libraryVersionName by extra("${versionMajor}.${versionMinor}.${versionPatch}.${versionAdapterPatch}")
val libraryVersionCode by extra((versionMajor * 1000000) + (versionMinor * 10000) + (versionPatch * 100) + versionAdapterPatch)

val libraryArtifactId by extra("vungle-adapter")

dependencies {
    implementation("androidx.annotation:annotation:1.5.0")

    //TODO: Replace with Line 50 when uploaded to Maven
    implementation("com.github.Vungle:vungle-android-sdk:6.12.1-RC3")

//    implementation("com.vungle:publisher-sdk-android:6.12.1")
    implementation("com.applovin:applovin-sdk:+@aar")
}
