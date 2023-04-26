package com.vungle.maxmediation.testapp.data.main

interface ListItem
{
    companion object
    {
        const val SECTION_HEADER = 0
        const val AD_ITEM = 1
        const val FOOTER = 2;
    }

    val type: Int
}
