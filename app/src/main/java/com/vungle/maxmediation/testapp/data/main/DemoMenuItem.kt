package com.vungle.maxmediation.testapp.data.main

import android.content.Intent

data class DemoMenuItem(val title: String, val intent: Intent?, val runnable:Runnable?, override val type: Int = ListItem.AD_ITEM) :
    ListItem
{
    constructor(title: String, intent: Intent) : this(title, intent, null)
    constructor(title: String, runnable:Runnable) : this(title, null, runnable)
}
