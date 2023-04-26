package com.vungle.maxmediation.testapp.ads.max.mrecs

import android.content.Intent
import com.vungle.maxmediation.testapp.ads.DemoMenuActivity
import com.vungle.maxmediation.testapp.data.main.DemoMenuItem

class MrecAdActivity : DemoMenuActivity() {
    override fun getListViewContents(): Array<DemoMenuItem> = arrayOf(
        DemoMenuItem("Programmatic MRECs (Bidding)", Intent(this, ProgrammaticMrecAdActivity::class.java)),
        DemoMenuItem("Layout Editor MRECs", Intent(this, LayoutEditorMrecAdActivity::class.java))
    )
}
