package com.app.lib_network.core

import com.alibaba.fastjson2.JSON
import java.lang.reflect.Type

class JsonConvert : Convert<Any> {
    override fun convert(response: String?, type: Type): Any? {
        return JSON.parseObject(response)?.getJSONObject("data")?.getString("data")?.let {
            JSON.parseObject(it, type)
        }
    }

    override fun convert(response: String?, clazz: Class<Any>): Any? {
        return JSON.parseObject(response)?.getJSONObject("data")?.getString("data")?.let {
            JSON.parseObject(it, clazz)
        }
    }
}