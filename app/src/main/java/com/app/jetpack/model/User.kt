package com.app.jetpack.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.jetpack.BR
import java.io.Serializable

data class User(
    var id: Int = 0,
    var userId: Long = 0L,
    var name: String? = "",
    var avatar: String? = "",
    var description: String? = "",
    var likeCount: Int = 0,
    var topCommentCount: Int = 0,
    var followCount: Int = 0,
    var followerCount: Int = 0,
    var qqOpenId: String = "",
    var expires_time: Long = 0L,
    var score: Int = 0,
    var historyCount: Int = 0,
    var commentCount: Int = 0,
    var favoriteCount: Int = 0,
    var feedCount: Int = 0,
) : Serializable, BaseObservable() {
    @get:Bindable
    var hasFollow: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
}