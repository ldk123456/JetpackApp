package com.app.jetpack.model

data class Destination(
    val id: Int,
    val className: String,
    val pageUrl: String,
    val needLogin: Boolean,
    val asStarter: Boolean,
    val isFragment: Boolean
)