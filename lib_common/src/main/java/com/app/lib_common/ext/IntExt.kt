@file:JvmMultifileClass
@file:JvmName("IntUtils")

package com.app.lib_common.ext

import com.app.lib_common.app.AppGlobals

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