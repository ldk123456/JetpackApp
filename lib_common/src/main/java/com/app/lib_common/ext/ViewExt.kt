@file:JvmMultifileClass
@file:JvmName("ViewUtils")

package com.app.lib_common.ext

import android.view.View
import com.app.lib_common.app.AppGlobals

inline val Boolean?.visibility: Int
    get() = if (this == true) View.VISIBLE else View.GONE

fun getScreenWidth() = (AppGlobals.context.resources.displayMetrics.widthPixels + 0.5f).toInt()

fun getScreenHeight() = (AppGlobals.context.resources.displayMetrics.heightPixels + 0.5f).toInt()

fun View?.setVisible(isVisible: Boolean) {
    this?.visibility = if (isVisible) View.VISIBLE else View.GONE
}