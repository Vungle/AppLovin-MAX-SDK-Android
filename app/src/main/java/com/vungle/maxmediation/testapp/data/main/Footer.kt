package com.vungle.maxmediation.testapp.data.main

import android.os.Build
import com.applovin.sdk.AppLovinSdk

data class Footer(override val type: Int = ListItem.FOOTER) : ListItem
{
    fun getAppDetails(): String
    {
        val sdkVersion: String = AppLovinSdk.VERSION
        val fields = Build.VERSION_CODES::class.java.fields
        var versionName = "UNKNOWN"
        fields.filter { it.getInt(Build.VERSION_CODES::class) == Build.VERSION.SDK_INT }
                .forEach { versionName = it.name }
        val apiLevel = Build.VERSION.SDK_INT

        return """
            SDK Version: $sdkVersion
            OS Version: $versionName(API $apiLevel)
            
            Language: Kotlin
            """.trimIndent()
    }
}
