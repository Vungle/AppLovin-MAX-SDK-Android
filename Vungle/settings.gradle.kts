include (":vungle")
rootProject.name="Vungle Max Mediation"
pluginManagement {
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven {"https://jitpack.io"}
        gradlePluginPortal()
    }
    plugins {
        id("com.android.library").version("7.2.2")
    }
//    resolutionStrategy {
//        eachPlugin {
//            if (requested.id.namespace == "com.android") {
//                useModule("com.android.tools.build:gradle:${requested.version}")
//            } else if(requested.id.namespace == "com.google.gms") {
//                useModule("com.google.gms:${requested.id.name}:${requested.version}")
//            }
//        }
//    }
//    dependencies {
//        classpath "com.android.tools.build:gradle:7.0.0-alpha08"
//        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30"
//    }
}

dependencyResolutionManagement {

    /**
     * The dependencyResolutionManagement { repositories {...}}
     * block is where you configure the repositories and dependencies used by
     * all modules in your project, such as libraries that you are using to
     * create your application. However, you should configure module-specific
     * dependencies in each module-level build.gradle file. For new projects,
     * Android Studio includes Google's Maven repository and the
     * Maven Central Repository by
     * default, but it does not configure any dependencies (unless you select a
     * template that requires some).
     */

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}