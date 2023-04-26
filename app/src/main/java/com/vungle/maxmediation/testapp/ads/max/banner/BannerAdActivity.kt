package com.vungle.maxmediation.testapp.ads.max.banner

import android.content.Intent

import com.vungle.maxmediation.testapp.ads.DemoMenuActivity
import com.vungle.maxmediation.testapp.data.main.DemoMenuItem

class BannerAdActivity : DemoMenuActivity()
{
    override fun getListViewContents(): Array<DemoMenuItem> = arrayOf(
        DemoMenuItem("Programmatic Banners (Bidding)", Intent(this, ProgrammaticBannerAdActivity::class.java)),
        DemoMenuItem("Layout Editor Banners", Intent(this, LayoutEditorBannerAdActivity::class.java))
    )
}
