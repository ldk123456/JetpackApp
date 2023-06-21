package com.app.jetpack.model

import java.io.Serializable

data class Ugc(
    var likeCount: Int = 0,
    var shareCount: Int = 0,
    var commentCount: Int = 0,
    var hasFavorite: Boolean = false,
    var hasLiked: Boolean = false,
    var hasdiss: Boolean = false
) : Serializable