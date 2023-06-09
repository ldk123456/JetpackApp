package com.app.lib_network.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "cache")
class Cache : Serializable {
    @PrimaryKey
    var key: String = ""

    var data: ByteArray = byteArrayOf()
}