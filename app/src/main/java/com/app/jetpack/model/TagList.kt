package com.app.jetpack.model

data class TagList(
    var id: Int = 0,
    var icon: String? = null,
    var background: String? = null,
    var activityIcon: String? = null,
    var title: String? = null,
    var intro: String? = null,
    var feedNum: Int = 0,
    var tagId: Int = 0,
    var enterNum: Int = 0,
    var followNum: Int = 0,
    var hasFollow: Boolean
)