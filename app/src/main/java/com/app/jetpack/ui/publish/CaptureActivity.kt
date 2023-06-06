package com.app.jetpack.ui.publish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.jetpack.R
import com.app.jetpack.core.PATH_MAIN_CAPTURE
import com.app.lib_nav_annotation.annotation.ActivityDestination

@ActivityDestination(PATH_MAIN_CAPTURE)
class CaptureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
    }
}