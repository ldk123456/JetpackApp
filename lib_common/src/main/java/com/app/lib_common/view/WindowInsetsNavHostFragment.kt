package com.app.lib_common.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment

class WindowInsetsNavHostFragment : NavHostFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return WindowInsetsFrameLayout(inflater.context).also {
            it.id = id
        }
    }
}