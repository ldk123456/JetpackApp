package com.app.lib_network

import android.annotation.SuppressLint
import android.util.Log
import com.app.lib_network.core.Convert
import com.app.lib_network.core.JsonConvert
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object ApiService {
    private const val TAG = "ApiService"

    val okHttpClient: OkHttpClient

    private lateinit var mBaseUrl: String
    lateinit var convert: Convert<Any>

    init {
        val interceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        okHttpClient = OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()
        val trustManagers = arrayOf(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    Log.i(TAG, "checkClientTrusted")
                }

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    Log.i(TAG, "checkServerTrusted")
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
        runCatching {
            val ssl = SSLContext.getInstance("SSL")
            ssl.init(null, trustManagers, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        }
    }

    fun init(baseUrl: String, convert: Convert<Any>? = null) {
        mBaseUrl = baseUrl
        this.convert = convert ?: JsonConvert()
    }
}