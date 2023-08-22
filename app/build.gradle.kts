plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
}

apply(plugin = "kotlin-android")
apply(plugin = "com.android.application")
apply(plugin = "kotlin-android-extensions")

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.vungle.games.tossacoin"
        minSdk = 16
        targetSdk = 32
        versionCode = 70000
        versionName = "7.0.0"
        versionNameSuffix = ""
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        java.sourceCompatibility = JavaVersion.VERSION_11
        java.targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        quiet = false
        abortOnError = false

        xmlReport = false

        htmlReport = true
        htmlOutput = file("${project.buildDir}/reports/lint/lint-report.html")
    }

    namespace = "com.applovin.enterprise.apps.testapp"
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}

dependencies {
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.core:core-ktx:1.9.0")

    implementation("com.adjust.sdk:adjust-android:4.28.7")
    implementation("com.android.installreferrer:installreferrer:2.2")

    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
    implementation("com.google.android.gms:play-services-appset:16.0.2")
    implementation("com.applovin:applovin-sdk:+@aar")
    implementation(project(":AppLovin-MAX-SDK-Android"))
//    implementation("com.github.vungle:AppLovin-MAX-SDK-Android:7.0.0.0-GA")

    implementation("com.microsoft.appcenter:appcenter-analytics:4.3.1")
    implementation("com.microsoft.appcenter:appcenter-crashes:4.3.1")
}