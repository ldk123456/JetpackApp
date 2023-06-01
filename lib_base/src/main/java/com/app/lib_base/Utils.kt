package com.app.lib_base

import java.io.Closeable

fun Closeable?.safeClose() {
    runCatching {
        this?.close()
    }.onFailure {
        it.printStackTrace()
    }
}