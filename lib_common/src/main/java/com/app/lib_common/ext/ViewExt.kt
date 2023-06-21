@file:JvmMultifileClass
@file:JvmName("ViewUtils")

package com.app.lib_common.ext

import android.view.View

inline val Boolean?.visibility: Int
    get() = if (this == true) View.VISIBLE else View.GONE
