package com.app.lib_common.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.app.lib_common.util.ViewHelper.setViewOutline

class RoundFragmentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    init {
        setViewOutline(attrs, defStyleAttr, defStyleRes)
    }
}