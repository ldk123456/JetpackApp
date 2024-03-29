package com.app.jetpack.model

import androidx.databinding.BaseObservable
import java.io.Serializable

data class Comment(
    var id: Int = 0,
    var itemId: Long = 0L,
    var commentId: Long = 0L,
    var userId: Long = 0L,
    var commentType: Int = 0,
    var createTime: Long = 0L,
    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var commentText: String? = "",
    var imageUrl: String? = "",
    var videoUrl: String? = "",
    var width: Int = 0,
    var height: Int = 0,
    var hasLiked: Boolean = false,
    var author: User? = null,
) : Serializable, BaseObservable() {
    companion object {
        const val COMMENT_TYPE_IMAGE_TEXT = 2
        const val COMMENT_TYPE_VIDEO = 3
    }

    var ugc: Ugc? = null
        get() {
            if (field == null) {
                field = Ugc()
            }
            return field!!
        }
}