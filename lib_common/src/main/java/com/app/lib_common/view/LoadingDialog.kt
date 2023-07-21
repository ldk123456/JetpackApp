package com.app.lib_common.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.app.lib_common.R
import com.app.lib_common.ext.dp
import com.app.lib_common.util.ViewHelper
import com.app.lib_common.util.ViewHelper.setViewOutline


class LoadingDialog : AlertDialog {
    private var loadingText: TextView? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    fun setLoadingText(loadingText: String?) {
        this.loadingText?.text = loadingText.orEmpty()
    }

    override fun show() {
        super.show()
        setContentView(R.layout.layout_loading_view)
        loadingText = findViewById<TextView>(R.id.loading_text)
        window?.apply {
            val attributes: WindowManager.LayoutParams = attributes
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes.gravity = Gravity.CENTER
            attributes.dimAmount = 0.5f
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<View>(R.id.loading_layout)?.setViewOutline(10.dp, ViewHelper.RADIUS_ALL)
            setAttributes(attributes)
        }
    }
}
