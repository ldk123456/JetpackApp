package com.app.lib_network.response

data class ApiResponse<T>(
    var success: Boolean = false,
    var status: Int = 0,
    var message: String = "",
    var body: T? = null
)