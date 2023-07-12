package com.app.lib_common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.lib_common.R
import com.app.lib_common.util.StatusBarUtil

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_JetpackApp)
        StatusBarUtil.fitSystemBar(this)
        super.onCreate(savedInstanceState)
    }
}