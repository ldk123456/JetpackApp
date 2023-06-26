@file:JvmMultifileClass
@file:JvmName("IntUtils")

package com.app.lib_common.ext

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.app.lib_common.app.AppGlobals
import java.lang.IllegalArgumentException

inline val Int.dp: Int
    get() = (AppGlobals.context.resources.displayMetrics.density * this + 0.5f).toInt()

fun Int?.convertFeedUgc(): String {
    return if (this == null) {
        ""
    } else if (this < 10000) {
        "$this"
    } else {
        "${this / 10000}万"
    }
}

inline val @receiver:DrawableRes Int.drawable: Drawable
    get() = ContextCompat.getDrawable(AppGlobals.context, this)
        ?: throw IllegalArgumentException("illegal drawable res id")