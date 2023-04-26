package com.vungle.maxmediation.testapp.data.main

data class SectionHeader(val title: String, override val type: Int = ListItem.SECTION_HEADER) :
    ListItem
