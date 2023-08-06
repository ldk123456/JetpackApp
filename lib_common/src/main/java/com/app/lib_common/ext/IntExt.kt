@file:JvmMultifileClass
@file:JvmName("IntUtils")

package com.app.lib_common.ext

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
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

@get:ColorInt
inline val @receiver:ColorRes Int.color: Int
    get() = ContextCompat.getColor(AppGlobals.context, this)

fun Int.convertTagFeedList(): String {
    return if (this < 10000) {
        "${this}人观看"
    } else {
        "${this / 10000}万人观看"
    }
}

fun Int.coverSpannable(intro: String?): CharSequence {
    val len = "$this".length
    val result = SpannableString("$this${intro.orEmpty()}")
    result.setSpan(ForegroundColorSpan(Color.BLACK), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    result.setSpan(AbsoluteSizeSpan(16, true), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    result.setSpan(StyleSpan(Typeface.BOLD), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return result
}