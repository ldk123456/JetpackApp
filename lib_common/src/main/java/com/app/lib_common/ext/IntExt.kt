package com.app.lib_common.ext

import com.app.lib_common.app.AppGlobals

inline val Int.dp: Int
    get() = (AppGlobals.context.resources.displayMetrics.density * this + 0.5f).toInt()