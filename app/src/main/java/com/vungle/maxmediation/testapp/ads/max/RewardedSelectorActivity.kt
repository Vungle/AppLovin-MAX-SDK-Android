package com.vungle.maxmediation.testapp.ads.max

import android.content.Intent
import com.vungle.maxmediation.testapp.ads.DemoMenuActivity
import com.vungle.maxmediation.testapp.data.main.DemoMenuItem

class RewardedSelectorActivity : DemoMenuActivity()
{
    private fun getIntent(isRealTime: Boolean) : Intent {
        val intent = Intent(this, RewardedAdActivity::class.java)
        intent.putExtra("isRealTime", isRealTime)
        return intent
    }

    override fun getListViewContents(): Array<DemoMenuItem> = arrayOf(
        DemoMenuItem("Waterfall Rewarded Interstitial", getIntent(false)),
        DemoMenuItem("Realtime Rewarded Interstitial", getIntent(true))
    )
}