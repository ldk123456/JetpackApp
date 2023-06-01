package com.app.jetpack

import android.app.Application
import com.app.jetpack.utils.AppGlobals

class MainApplication :Application() {
    override fun onCreate() {
        super.onCreate()
        AppGlobals.init(this)
    }
}