package com.app.lib_common.ext

import android.graphics.Color

inline val String.color: Int
    get() = Color.parseColor(this)