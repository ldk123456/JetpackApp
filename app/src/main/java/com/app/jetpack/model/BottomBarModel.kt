package com.app.jetpack.model

data class BottomBarModel(
    var activeColor: String = "",
    var inActiveColor: String = "",
    // 底部导航栏默认选中项
    var selectTab: Int = 0,
    var tabs: List<BottomBarTab> = listOf()
)

data class BottomBarTab(
    var size: Int = 0,
    var enable: Boolean = true,
    var index: Int = 0,
    var pageUrl: String = "",
    var title: String = "",
    var tintColor: String = ""
)