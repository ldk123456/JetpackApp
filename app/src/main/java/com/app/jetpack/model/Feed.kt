package com.app.jetpack.model

import java.io.Serializable

data class Feed(
    var id: Int = 0,
    var itemId: Long = 0L,
    var itemType: Int = 0,
    var createTime: Int = 0,
    var duration: Double = 0.0,
    var feeds_text: String? = "",
    var authorId: Long = 0L,
    var activityIcon: String? = "",
    var activityText: String? = "",
    var width: Int = 0,
    var height: Int = 0,
    var url: String? = "",
    var cover: String? = "",
    var author: User? = null,
    var topComment: Comment? = null,
    var ugc: Ugc? = null
) : Serializable