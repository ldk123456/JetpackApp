package com.app.lib_network.request

import com.app.lib_network.core.UrlCreator

class GetRequest<T>(url: String) : Request<T, GetRequest<T>>(url) {
    override fun generateRequest(builder: okhttp3.Request.Builder): okhttp3.Request {
        return builder.get().url(UrlCreator.createUrlFromParams(url, params)).build()
    }
}