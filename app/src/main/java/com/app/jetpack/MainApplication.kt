package com.app.jetpack

import android.app.Application
import com.app.lib_common.app.AppGlobals

class MainApplication :Application() {
    override fun onCreate() {
        super.onCreate()
        AppGlobals.init(this)
    }
}