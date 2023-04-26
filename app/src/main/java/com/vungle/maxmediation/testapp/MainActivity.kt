package com.vungle.maxmediation.testapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.applovin.enterprise.apps.testapp.R
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.google.android.material.snackbar.Snackbar
import com.vungle.maxmediation.testapp.ads.max.InterstitalSelectorActivity
import com.vungle.maxmediation.testapp.ads.max.RewardedSelectorActivity
import com.vungle.maxmediation.testapp.ads.max.banner.BannerAdActivity
import com.vungle.maxmediation.testapp.ads.max.mrecs.MrecAdActivity
import com.vungle.maxmediation.testapp.ads.max.nativead.NativeAdActivity
import com.vungle.maxmediation.testapp.data.main.DemoMenuItem
import com.vungle.maxmediation.testapp.data.main.Footer
import com.vungle.maxmediation.testapp.data.main.ListItem
import com.vungle.maxmediation.testapp.data.main.SectionHeader
import com.vungle.maxmediation.testapp.ui.MainRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity(),
        MainRecyclerViewAdapter.OnMainListItemClickListener
{
    private var isFABOpen : Boolean = false

    private lateinit var muteToggleMenuItem: MenuItem

    private fun generateMainListItems(): List<ListItem>
    {
        val items: MutableList<ListItem> =
                ArrayList()
        items.add(SectionHeader("Vungle MAX AdUnits"))
        items.add(DemoMenuItem("Interstitial", Intent(this, InterstitalSelectorActivity::class.java)))
        items.add(DemoMenuItem("Rewarded", Intent(this, RewardedSelectorActivity::class.java)))
        items.add(DemoMenuItem("Banners", Intent(this, BannerAdActivity::class.java)))
        items.add(DemoMenuItem("MRECs", Intent(this, MrecAdActivity::class.java)))
        items.add(DemoMenuItem("Native Ads", Intent(this, NativeAdActivity::class.java)))
        items.add(SectionHeader("Debugging and Logging"))
        items.add(DemoMenuItem("Launch Mediation Debugger", Runnable({ AppLovinSdk.getInstance(applicationContext).showMediationDebugger() })))
        items.add(Footer())
        return items
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val recyclerViewAdapter = MainRecyclerViewAdapter(generateMainListItems(), this, this)
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)

        mainRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = manager
            addItemDecoration(decoration)
            itemAnimator = DefaultItemAnimator()
            adapter = recyclerViewAdapter
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return true
            }
        })

        // Check that SDK key is present in Android Manifest
        checkSdkKey()

        fab.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        fab1.setOnClickListener {
            closeFABMenu()
            onCreatePrivacyDialog("COPPA").show()
        }

        fab2.setOnClickListener {
            closeFABMenu()
            onCreatePrivacyDialog("CCPA").show()
        }

        fab3.setOnClickListener {
            closeFABMenu()
            onCreatePrivacyDialog("GDPR").show()
        }

        fab4.setOnClickListener {
            closeFABMenu()
            val userService = AppLovinSdk.getInstance(this).userService
            userService.showConsentDialog(this) { }
        }

        AppLovinSdk.getInstance( applicationContext ).settings.setVerboseLogging( true )

        // Initialize the AppLovin SDK
        AppLovinSdk.getInstance(this).mediationProvider = AppLovinMediationProvider.MAX
        AppLovinSdk.getInstance(this).initializeSdk {
            // AppLovin SDK is initialized, start loading ads now or later if ad gate is reached
        }
    }

    override fun onItemClicked(item: ListItem)
    {
        if (item is DemoMenuItem)
        {
            if (item.intent != null)
            {
                startActivity(item.intent);
            }
            else if (item.runnable != null)
            {
                item.runnable.run();
            }
        }
    }

    private fun checkSdkKey()
    {
        val sdkKey = AppLovinSdk.getInstance(applicationContext).sdkKey
        if ("YOUR_SDK_KEY".equals(sdkKey, ignoreCase = true))
        {
            AlertDialog.Builder(this)
                    .setTitle("ERROR")
                    .setMessage("Please update your sdk key in the manifest file.")
                    .setCancelable(false)
                    .setNeutralButton("OK", null)
                    .show()
        }
    }

    // Mute Toggling

    /**
     * Toggling the sdk mute setting will affect whether your video ads begin in a muted state or not.
     */
    private fun toggleMute()
    {
        val sdk = AppLovinSdk.getInstance(applicationContext)
        sdk.settings.isMuted = !sdk.settings.isMuted
        muteToggleMenuItem.icon = getMuteIconForCurrentSdkMuteSetting()
    }

    private fun getMuteIconForCurrentSdkMuteSetting(): Drawable
    {
        val sdk = AppLovinSdk.getInstance(applicationContext)
        val drawableId = if (sdk.settings.isMuted) R.drawable.mute else R.drawable.unmute

        if (Build.VERSION.SDK_INT >= 22)
        {
            return resources.getDrawable(drawableId, theme)
        }
        else
        {
            @Suppress("DEPRECATION")
            return resources.getDrawable(drawableId)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean
    {
        muteToggleMenuItem = menu.findItem(R.id.action_toggle_mute).apply {
            icon = getMuteIconForCurrentSdkMuteSetting()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if (item.itemId == R.id.action_toggle_mute)
        {
            toggleMute()
        }

        return true
    }


    private fun showFABMenu() {
        isFABOpen = true
        fab1.animate().translationY(-resources.getDimension(R.dimen.standard_55))
        fab2.animate().translationY(-resources.getDimension(R.dimen.standard_105))
        fab3.animate().translationY(-resources.getDimension(R.dimen.standard_155))
        fab4.animate().translationY(-resources.getDimension(R.dimen.standard_205))
    }

    private fun closeFABMenu() {
        isFABOpen = false
        fab1.animate().translationY(0f)
        fab2.animate().translationY(0f)
        fab3.animate().translationY(0f)
        fab4.animate().translationY(0f)
    }

    private fun onCreatePrivacyDialog(consentType :String): Dialog {
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
}
