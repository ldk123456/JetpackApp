package com.app.lib_common.ext

import android.os.Bundle
import androidx.fragment.app.Fragment

fun <T> Any?.safeAs(): T? = this as? T

fun <T : Fragment> T.withBundle(block: Bundle.() -> Unit): T {
    val bundle = Bundle()
    bundle.block()
    arguments = bundle
    return this
}