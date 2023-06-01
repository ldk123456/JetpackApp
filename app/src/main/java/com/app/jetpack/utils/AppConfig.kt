package com.app.jetpack.utils

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.Destination
import com.app.lib_base.DESTINATION_OUTPUT_FILE_NAME
import java.io.BufferedReader
import java.io.InputStreamReader

object AppConfig {
    private val sDestConfig = HashMap<String, Destination>()

    fun getDestConfig(): Map<String, Destination> {
        if (sDestConfig.isEmpty()) {
            with(parseFile(DESTINATION_OUTPUT_FILE_NAME)) {
                val map = JSON.parseObject(this, object : TypeReference<HashMap<String, Destination>>() {})
                sDestConfig.putAll(map)
            }
        }
        return sDestConfig
    }

    private fun parseFile(fileName: String): String {
        return AppGlobals.context.assets.open(fileName).use {
            BufferedReader(InputStreamReader(it)).readText()
        }
    }
}