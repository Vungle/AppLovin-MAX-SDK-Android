include ':vungle'
rootProject.name='Vungle Max Mediation'
pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven {"https://jitpack.io"}
    }
}