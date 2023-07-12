package com.app.jetpack.ui.publish

import android.os.Bundle
import com.app.jetpack.R
import com.app.jetpack.core.PATH_MAIN_CAPTURE
import com.app.lib_common.base.BaseActivity
import com.app.lib_nav_annotation.annotation.ActivityDestination

@ActivityDestination(PATH_MAIN_CAPTURE)
class CaptureActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
    }
}