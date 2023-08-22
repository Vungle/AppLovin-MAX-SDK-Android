package com.vungle.maxmediation.testapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applovin.enterprise.apps.testapp.BuildConfig
import com.applovin.enterprise.apps.testapp.R
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkUtils
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.android.synthetic.main.activity_splash.*
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        btn_max_consent_flow.setOnClickListener {
            val userService = AppLovinSdk.getInstance(this).userService
            userService.showConsentDialog(this) { }
        }

        btn_gdpr.setOnClickListener {
            onCreateDialog("GDPR").show()
        }

        btn_ccpa.setOnClickListener {
            onCreateDialog("CCPA").show()
        }

        btn_coppa.setOnClickListener {
            onCreateDialog("COPPA").show()
        }

        btn_start.setOnClickListener {
            startMainActivity()
            btn_start.isClickable = false
        }

        val adapterVer = com.applovin.mediation.adapters.vungle.BuildConfig.VERSION_NAME
        val vngSdkVersion =
            getString("com.vungle.ads.BuildConfig", "VERSION_NAME")

        titleVersion.text =
            "Test app ver: ${BuildConfig.VERSION_NAME}\nAdapter ver: $adapterVer\nVng SDK ver: $vngSdkVersion"
    }

    private fun getString(className: String, fieldName: String): String {
        return try {
            val clz = Class.forName(className)
            val field: Field = clz.getField(fieldName)
            field.isAccessible = true
            field.get(null) as String
        } catch (e: Exception) {
            ""
        }
    }

    private fun onCreateDialog(consentType: String): Dialog {
        return this.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("$consentType Consent")
                .setItems(
                    R.array.consent_options,
                    DialogInterface.OnClickListener { dialog, which ->
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (consentType == "COPPA") {
                            AppLovinPrivacySettings.setIsAgeRestrictedUser(which == 0, this)
                        }
                        if (consentType == "CCPA") {
                            AppLovinPrivacySettings.setDoNotSell(which == 0, this)
                        }
                        if (consentType == "GDPR") {
                            AppLovinPrivacySettings.setHasUserConsent(which == 0, this)
                        }
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    fun startMainActivity() {

        AppLovinSdkUtils.runOnUiThreadDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }, TimeUnit.SECONDS.toMillis(2))

        AppCenter.start(
            application, "fd0b5f0c-5c95-4b13-9677-379c2d5958e9",
            Analytics::class.java, Crashes::class.java
        )
    }
}
