package com.app.lib_network.core

import java.net.URLEncoder

object UrlCreator {
    fun createUrlFromParams(url: String, params: Map<String, Any?>): String {
        return with(StringBuilder()) {
            append(url)
            if (url.indexOf("?") > 0 || url.indexOf("&") > 0) {
                append("&")
            } else {
                append("?")
            }
            params.forEach { (k, v) ->
                append(k).append("=")
                    .append(URLEncoder.encode(v.toString(), "UTF-8"))
                    .append("&")
            }
            deleteCharAt(lastIndex)
            toString()
        }
    }
}