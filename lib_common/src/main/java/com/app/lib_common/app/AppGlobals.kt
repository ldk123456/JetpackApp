package com.app.lib_common.app

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppGlobals {
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }
}