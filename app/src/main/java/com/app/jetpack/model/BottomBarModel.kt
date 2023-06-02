package com.app.jetpack.model

data class BottomBarModel(
    var activeColor: String = "",
    var inActiveColor: String = "",
    var tabs: List<BottomBarTab> = listOf()
)

data class BottomBarTab(
    var size: Int = 0,
    var enable: Boolean = true,
    var index: Int = 0,
    var pageUrl: String = "",
    var title: String = ""
)