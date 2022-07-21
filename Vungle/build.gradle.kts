plugins {
    id("signing")
    id("maven-publish")
    id("com.android.library")
}
apply(plugin = "com.android.library")

android {
    defaultConfig {
        compileSdkVersion(31)
        buildConfigField("int", "VERSION_CODE", "06110000")
        buildConfigField("String", "VERSION_NAME", "\"6.11.0.0\"");
    }
}


private val versionMajor = 6
private val versionMinor = 12
private val versionPatch = 0
private val versionAdapterPatch = 0

val libraryVersionName by extra("${versionMajor}.${versionMinor}.${versionPatch}.${versionAdapterPatch}")
val libraryVersionCode by extra((versionMajor * 1000000) + (versionMinor * 10000) + (versionPatch * 100) + versionAdapterPatch)

val libraryArtifactId by extra("vungle-adapter")
val libraryGroupId by extra("com.applovin.mediation")

dependencies {

//    implementation("com.vungle:publisher-sdk-android:6.11.0")

    //TODO: Replace this with maven repo as above when available
    implementation("com.github.Vungle:vungle-android-sdk:6.12.0-QA2")
    implementation("com.applovin:applovin-sdk:+@aar")
}
