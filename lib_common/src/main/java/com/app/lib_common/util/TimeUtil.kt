package com.app.lib_common.util

import java.util.Calendar

object TimeUtil {
    @JvmStatic
    fun calculate(time: Long): String {
        val t = if ("$time".length < 13) {
            time * 1000
        } else {
            time
        }
        val diff = (Calendar.getInstance().timeInMillis - t) / 1000
        return if (diff < 60) {
            "${diff}秒前"
        } else if (diff < 60 * 60) {
            "${diff / 60}分钟前"
        } else if (diff < 60 * 60 * 24) {
            "${diff / 60 / 60}小时前"
        } else {
            "${diff / 60 / 60 / 24}天前"
        }
    }
}