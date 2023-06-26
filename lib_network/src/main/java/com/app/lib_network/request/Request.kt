package com.app.lib_network.request

import androidx.annotation.WorkerThread
import androidx.arch.core.executor.ArchTaskExecutor
import com.app.lib_common.app.AppGlobals
import com.app.lib_network.response.ApiResponse
import com.app.lib_network.ApiService
import com.app.lib_network.cache.CacheManager
import com.app.lib_network.core.CacheStrategy
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.core.UrlCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
abstract class Request<T, R : Request<T, R>>(
    protected var url: String = ""
) : Cloneable {
    companion object {
        private const val TAG = "Request"
    }

    private val headers = HashMap<String, String>()
    protected val params = HashMap<String, Any>()

    private var mCacheKey: String = TAG
    private var mType: Type? = null
    private var mClass: Class<T>? = null
    @CacheStrategy
    private var mCacheStrategy: Int = CacheStrategy.NET_ONLY

    private val newCall: Call
        get() {
            val builder = okhttp3.Request.Builder()
            addHeaders(builder)
            val request = generateRequest(builder)
            return ApiService.okHttpClient.newCall(request)
        }

    fun addHeader(key: String, value: String): R {
        headers[key] = value
        return this as R
    }

    fun addParam(key: String, value: Any?): R {
        value?.let { v ->
            runCatching {
                if (v.javaClass == String::class.java) {
                    params[key] = v
                } else {
                    (v.javaClass.getField("TYPE").get(null) as? Class<*>)
                        ?.takeIf { it.isPrimitive }?.let {
                            params[key] = v
                        }
                }
            }
        }
        return this as R
    }

    fun cacheStrategy(@CacheStrategy cacheStrategy: Int): R {
        mCacheStrategy = cacheStrategy
        return this as R
    }

    fun cacheKey(key: String): R {
        mCacheKey = key
        return this as R
    }

    fun responseType(type: Type? = null, clazz: Class<T>? = null): R {
        mType = type
        mClass = clazz
        return this as R
    }

    private fun addHeaders(builder: okhttp3.Request.Builder) {
        headers.forEach { (k, v) ->
            builder.addHeader(k, v)
        }
    }

    protected abstract fun generateRequest(builder: okhttp3.Request.Builder): okhttp3.Request

    fun execute(callback: JsonCallback<T>) {
        if (mCacheStrategy != CacheStrategy.NET_ONLY) {
            AppGlobals.applicationScope.launch {
                withContext(Dispatchers.IO) {
                    readCache()
                }.takeIf {
                    it.body != null
                }?.let {
                    callback.onCacheSuccess(it)
                }
            }
        }
        if (mCacheStrategy == CacheStrategy.CACHE_ONLY) {
            return
        }
        newCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(ApiResponse(message = e.message.orEmpty()))
            }

            override fun onResponse(call: Call, response: Response) {
                val result = parseResponse(response, callback)
                if (result.success) {
                    callback.onSuccess(result)
                } else {
                    callback.onError(result)
                }
            }
        })
    }

    @WorkerThread
    fun execute(): ApiResponse<T> {
        if (mType == null && mClass == null) {
            throw RuntimeException("同步方法,type|classType类型必须设置")
        }
        if (mCacheStrategy == CacheStrategy.CACHE_ONLY) {
            return readCache()
        }
        return runCatching {
            parseResponse(newCall.execute())
        }.getOrElse {
            ApiResponse(message = it.message.orEmpty())
        }
    }

    suspend fun suspendExecute() = withContext(Dispatchers.IO) {
        execute()
    }

    private fun parseResponse(response: Response, callback: JsonCallback<T>? = null): ApiResponse<T> {
        val result = ApiResponse<T>(status = response.code, success = response.isSuccessful)
        runCatching {
            val content = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                if (callback != null) {
                    (callback.javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.firstOrNull()
                        ?.let {
                            result.body = ApiService.convert.convert(content, it) as? T
                        }
                } else if (mType != null) {
                    result.body = ApiService.convert.convert(content, mType!!) as? T
                } else if (mClass != null) {
                    result.body = ApiService.convert.convert(content, mClass!!) as? T
                } else {
                    result.success = false
                    result.message = content
                }
            } else {
                result.message = content
            }
        }.onFailure {
            result.message = it.message.orEmpty()
            result.success = false
            result.status = 0
        }

        if (mCacheStrategy != CacheStrategy.NET_ONLY && result.success && result.body is Serializable) {
            saveCache(result.body)
        }

        return result
    }

    private fun readCache(): ApiResponse<T> {
        val key = mCacheKey.takeIf { it.isNotEmpty() } ?: UrlCreator.createUrlFromParams(url, params)
        val cache = CacheManager.getCache<T>(key)
        return ApiResponse(status = 304, message = "缓存获取成功", body = cache, success = true)
    }

    private fun saveCache(body: T?) {
        body ?: return
        val key = mCacheKey.takeIf { it.isNotEmpty() } ?: UrlCreator.createUrlFromParams(url, params)
        CacheManager.save(key, body)
    }

    public override fun clone(): R {
        return super.clone() as R
    }
}