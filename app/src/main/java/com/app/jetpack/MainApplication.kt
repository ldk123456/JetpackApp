package com.app.jetpack

import android.app.Application
import com.app.lib_common.app.AppGlobals
import com.app.lib_network.ApiService

class MainApplication :Application() {
    override fun onCreate() {
        super.onCreate()
        AppGlobals.init(this)
        ApiService.init("http://123.56.232.18:8080/serverdemo")
    }
}