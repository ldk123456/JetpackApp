package com.app.jetpack.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.jetpack.BR
import java.io.Serializable

data class Ugc(
    var likeCount: Int = 0,
    var commentCount: Int = 0,
) : Serializable, BaseObservable() {
    @get:Bindable
    var shareCount: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }

    @get:Bindable
    var hasdiss: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                hasLiked = false
            }
            field = value
            notifyPropertyChanged(BR._all)
        }

    @get:Bindable
    var hasLiked: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                likeCount += 1
                hasdiss = false
            } else {
                likeCount -= 1
            }
            field = value
            notifyPropertyChanged(BR._all)
        }

    @get:Bindable
    var hasFavorite: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
}