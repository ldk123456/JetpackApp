package com.app.lib_network.core

import okio.ByteString.Companion.encodeUtf8

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
                    .append(v.toString().encodeUtf8().toString())
                    .append("&")
            }
            deleteCharAt(lastIndex)
            toString()
        }
    }
}