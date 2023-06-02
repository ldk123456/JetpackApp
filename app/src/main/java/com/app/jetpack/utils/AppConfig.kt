package com.app.jetpack.utils

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.BottomBarModel
import com.app.jetpack.model.Destination
import com.app.lib_base.BOTTOM_BAR_CONFIG_FILE_NAME
import com.app.lib_base.DESTINATION_OUTPUT_FILE_NAME
import java.io.BufferedReader
import java.io.InputStreamReader

object AppConfig {
    private val sDestConfig = HashMap<String, Destination>()

    private var sBottomBarConfig: BottomBarModel? = null

    fun getDestConfig(): Map<String, Destination> {
        if (sDestConfig.isEmpty()) {
            with(parseFile(DESTINATION_OUTPUT_FILE_NAME)) {
                val map = JSON.parseObject(this, object : TypeReference<HashMap<String, Destination>>() {})
                sDestConfig.putAll(map)
            }
        }
        return sDestConfig
    }

    fun getBottomBarConfig(): BottomBarModel? {
        if (sBottomBarConfig == null) {
            with(parseFile(BOTTOM_BAR_CONFIG_FILE_NAME)) {
                sBottomBarConfig = JSON.parseObject(this, BottomBarModel::class.java)
            }
        }
        return sBottomBarConfig
    }

    private fun parseFile(fileName: String): String {
        return AppGlobals.context.assets.open(fileName).use {
            BufferedReader(InputStreamReader(it)).readText()
        }
    }
}