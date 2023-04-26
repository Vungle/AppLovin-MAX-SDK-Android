package com.vungle.maxmediation.testapp.ads.max

import android.content.Intent
import com.vungle.maxmediation.testapp.ads.DemoMenuActivity
import com.vungle.maxmediation.testapp.data.main.DemoMenuItem

class InterstitalSelectorActivity : DemoMenuActivity()
{
    private fun getIntent(isRealTime: Boolean) : Intent {
        val intent = Intent(this, InterstitialAdActivity::class.java)
        intent.putExtra("isRealTime", isRealTime)
        return intent
    }

    override fun getListViewContents(): Array<DemoMenuItem> = arrayOf(
        DemoMenuItem("Waterfall Interstitial",
            getIntent(false)),
        DemoMenuItem("RealTime Interstitial",
            getIntent(true))
    )

}