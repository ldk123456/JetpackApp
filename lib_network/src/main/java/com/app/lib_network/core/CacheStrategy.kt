package com.app.lib_network.core

import androidx.annotation.IntDef
import com.app.lib_network.core.CacheStrategy.Companion.CACHE_ONLY
import com.app.lib_network.core.CacheStrategy.Companion.CACHE_FIRST
import com.app.lib_network.core.CacheStrategy.Companion.NET_ONLY
import com.app.lib_network.core.CacheStrategy.Companion.NET_CACHE

@IntDef(CACHE_ONLY, CACHE_FIRST, NET_ONLY, NET_CACHE)
@Retention(AnnotationRetention.SOURCE)
annotation class CacheStrategy {
    companion object {
        //仅仅只访问本地缓存，即便本地缓存不存在，也不会发起网络请求
        const val CACHE_ONLY = 1
        //先访问缓存，同时发起网络的请求，成功后缓存到本地
        const val CACHE_FIRST = 2
        //仅仅只访问服务器，不存任何存储
        const val NET_ONLY = 3
        //先访问网络，成功后缓存到本地
        const val NET_CACHE = 4
    }
}