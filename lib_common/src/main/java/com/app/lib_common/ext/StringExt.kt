@file:JvmMultifileClass
@file:JvmName("StringUtils")

package com.app.lib_common.ext

import android.graphics.Color
import android.view.View

inline val String.color: Int
    get() = Color.parseColor(this)

inline val String?.visibility: Int
    get() = if (isNullOrEmpty()) View.GONE else View.VISIBLE