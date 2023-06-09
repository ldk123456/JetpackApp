package com.app.lib_network.cache

import com.app.lib_base.safeClose
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


object CacheManager {
    private fun toObject(data: ByteArray?): Any? {
        if (data == null || data.isEmpty()) {
            return null
        }
        var bais: ByteArrayInputStream? = null
        var ois: ObjectInputStream? = null
        try {
            bais = ByteArrayInputStream(data)
            ois = ObjectInputStream(bais)
            return ois.readObject()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bais.safeClose()
            ois.safeClose()
        }
        return null
    }

    private fun <T> toByteArray(body: T?): ByteArray {
        if (body == null) {
            return byteArrayOf()
        }
        var baos: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            oos = ObjectOutputStream(baos)
            oos.writeObject(body)
            oos.flush()
            return baos.toByteArray() ?: byteArrayOf()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            baos.safeClose()
            oos.safeClose()
        }
        return byteArrayOf()
    }

    fun <T> delete(key: String?, body: T?) {
        if (key.isNullOrEmpty() || body == null) {
            return
        }
        val cache = Cache()
        cache.key = key
        cache.data = toByteArray(body)
        CacheDatabase.get().cacheDao().delete(cache)
    }

    fun <T> save(key: String?, body: T?) {
        if (key.isNullOrEmpty() || body == null) {
            return
        }
        val cache = Cache()
        cache.key = key
        cache.data = toByteArray(body)
        CacheDatabase.get().cacheDao().save(cache)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String?): T? {
        if (key.isNullOrEmpty()) {
            return null
        }
        return CacheDatabase.get().cacheDao().getCache(key)?.data?.let { toObject(it) } as? T
    }
}