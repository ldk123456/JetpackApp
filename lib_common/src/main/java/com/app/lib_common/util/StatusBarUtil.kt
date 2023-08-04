package com.app.lib_common.util

import android.graphics.Color
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

object StatusBarUtil {
    fun fitSystemBar(activity: AppCompatActivity) {
        val window = activity.window
        val decorView = window?.decorView
        if (window == null || decorView == null) {
            return
        }

        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    fun lightStatusBar(activity: FragmentActivity, light: Boolean) {
        val window = activity.window ?: return
        val decorView = window.decorView
        var visibility = decorView.systemUiVisibility
        if (light) {
            visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            visibility = visibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = visibility
    }
}