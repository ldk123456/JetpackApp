package com.app.lib_common.app

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@SuppressLint("StaticFieldLeak")
object AppGlobals {
    lateinit var context: Context

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun init(context: Context) {
        this.context = context
    }
}