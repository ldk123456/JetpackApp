package com.app.lib_network.core

import com.app.lib_network.response.ApiResponse

interface JsonCallback<T> {
    fun onSuccess(response: ApiResponse<T>) {}

    fun onError(response: ApiResponse<T>) {}

    fun onCacheSuccess(response: ApiResponse<T>) {}
}