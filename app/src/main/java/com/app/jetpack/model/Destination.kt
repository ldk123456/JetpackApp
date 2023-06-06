package com.app.jetpack.model

data class Destination(
    var id: Int = 0,
    var className: String = "",
    var pageUrl: String = "",
    var needLogin: Boolean = false,
    var asStarter: Boolean = false,
    var isFragment: Boolean = true
) {
    // JSON 序列化需要
    fun setIsFragment(isFragment: Boolean) {
        this.isFragment = isFragment
    }
}