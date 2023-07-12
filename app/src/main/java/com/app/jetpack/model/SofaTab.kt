package com.app.jetpack.model

data class SofaTab(
    var activeSize: Int = 0,
    var normalSize: Int = 0,
    var activeColor: String? = null,
    var normalColor: String? = null,
    var select: Int = 0,
    var tabGravity: Int = 0,
    var tabs: List<Tab?>? = null
)

data class Tab(
    var title: String? = null,
    var index: Int = 0,
    var tag: String? = null,
    var enable: Boolean = true
)