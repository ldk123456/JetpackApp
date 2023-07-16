package com.app.jetpack.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import java.io.Serializable

data class Feed(
    var id: Int = 0,
    var itemId: Long = 0L,
    var itemType: Int = 0,
    var createTime: Long = 0L,
    var duration: Double = 0.0,
    var feeds_text: String? = "",
    var authorId: Long = 0L,
    var activityIcon: String? = "",
    var activityText: String? = "",
    var width: Int = 0,
    var height: Int = 0,
    var url: String? = "",
    var cover: String? = "",
    var topComment: Comment? = null,
) : Serializable, BaseObservable() {
    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
    }

    @get:Bindable
    var ugc: Ugc? = null
        get() {
            if (field == null) {
                field = Ugc()
            }
            return field!!
        }

    @get:Bindable
    var author: User? = null
}