package com.app.jetpack.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.app.jetpack.BR

data class TagList(
    var id: Int = 0,
    var icon: String? = null,
    var background: String? = null,
    var activityIcon: String? = null,
    var title: String? = null,
    var intro: String? = null,
    var feedNum: Int = 0,
    var tagId: Long = 0L,
    var enterNum: Int = 0,
    var followNum: Int = 0,
    ) : BaseObservable() {
    @get:Bindable
    var hasFollow: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
}