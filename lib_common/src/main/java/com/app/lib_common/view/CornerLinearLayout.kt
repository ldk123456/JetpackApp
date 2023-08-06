package com.app.lib_common.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.app.lib_common.util.ViewHelper.setViewOutline

class CornerLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    init {
        setViewOutline(attrs, defStyleAttr, defStyleRes)
    }
}