plugins {
    id("signing")
    id("maven-publish")
    id("com.android.library")
}
apply(plugin = "com.android.library")

android {
    defaultConfig {
        compileSdkVersion(31)
        buildConfigField("int", "VERSION_CODE", "06120001")
        buildConfigField("String", "VERSION_NAME", "\"6.12.0.1\"");
    }
}


private val versionMajor = 6
private val versionMinor = 12
private val versionPatch = 0
private val versionAdapterPatch = 1

val libraryVersionName by extra("${versionMajor}.${versionMinor}.${versionPatch}.${versionAdapterPatch}")
val libraryVersionCode by extra((versionMajor * 1000000) + (versionMinor * 10000) + (versionPatch * 100) + versionAdapterPatch)

val libraryArtifactId by extra("vungle-adapter")
val libraryGroupId by extra("com.applovin.mediation")

dependencies {
    implementation("com.vungle:publisher-sdk-android:6.12.0")
    implementation("com.applovin:applovin-sdk:+@aar")
}
