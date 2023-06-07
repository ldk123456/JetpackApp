package com.app.lib_network.request

import okhttp3.FormBody

class PostRequest<T>(url: String) : Request<T, PostRequest<T>>(url) {
    override fun generateRequest(builder: okhttp3.Request.Builder): okhttp3.Request {
        val body = FormBody.Builder()
        params.forEach { (k, v) ->
            body.add(k, v.toString())
        }
        return builder.url(url).post(body.build()).build()
    }
}