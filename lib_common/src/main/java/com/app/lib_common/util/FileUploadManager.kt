package com.app.lib_common.util

import android.util.Log
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.GetObjectRequest
import com.alibaba.sdk.android.oss.model.GetObjectResult
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.app.lib_common.app.AppGlobals


class FileUploadManager {

    companion object {
        private var oss: OSSClient? = null
        private const val ALIYUN_BUCKET_URL = "https://pipijoke.oss-cn-hangzhou.aliyuncs.com/"
        private const val BUCKET_NAME = "pipijoke"
        private const val END_POINT = "http://oss-cn-hangzhou.aliyuncs.com"
        private const val AUTH_SERVER_URL = "http://123.56.232.18:7080/"

        init {
            val credentialProvider: OSSCredentialProvider = OSSAuthCredentialsProvider(AUTH_SERVER_URL)
            //该配置类如果不设置，会有默认配置，具体可看该类
            val conf = ClientConfiguration()
            conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
            conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
            conf.maxConcurrentRequest = 5 // 最大并发请求数，默认5个
            conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
            OSSLog.disableLog() //这个开启会支持写入手机sd卡中的一份日志文件位置在SDCard_path\OSSLog\logs.csv
            oss = OSSClient(AppGlobals.context, END_POINT, credentialProvider, conf)
        }

        //同步
        @Throws(ClientException::class, ServiceException::class)
        fun upload(bytes: ByteArray?): String? {
            val objectKey = System.currentTimeMillis().toString()
            val request = PutObjectRequest(BUCKET_NAME, objectKey, bytes)
            val result = oss!!.putObject(request)
            return if (result.statusCode == 200) {
                ALIYUN_BUCKET_URL + objectKey
            } else {
                null
            }
        }

        //异步
        fun upload(bytes: ByteArray?, callback: UploadCallback?) {
            val objectKey = System.currentTimeMillis().toString()
            val request = PutObjectRequest(BUCKET_NAME, objectKey, bytes)
            upload(request, callback)
        }

        //同步
        fun upload(filePath: String): String? {
            val objectKey = filePath.substring(filePath.lastIndexOf("/") + 1)
            val request = PutObjectRequest(BUCKET_NAME, objectKey, filePath)
            var result: PutObjectResult? = null
            try {
                result = oss!!.putObject(request)
            } catch (e: ClientException) {
                e.printStackTrace()
            } catch (e: ServiceException) {
                e.printStackTrace()
            }
            return if (result != null && result.statusCode == 200) {
                ALIYUN_BUCKET_URL + objectKey
            } else {
                null
            }
        }

        //异步
        fun upload(filePath: String, callback: UploadCallback?) {
            val objectKey = filePath.substring(filePath.lastIndexOf("/") + 1)
            val request = PutObjectRequest(BUCKET_NAME, objectKey, filePath)
            upload(request, callback)
        }

        private fun upload(put: PutObjectRequest, callback: UploadCallback?) {
            put.progressCallback =
                OSSProgressCallback<PutObjectRequest?> { request, currentSize, totalSize ->
                    Log.e(
                        "PutObject",
                        "currentSize: $currentSize totalSize: $totalSize"
                    )
                }
            val task: OSSAsyncTask<*> =
                oss!!.asyncPutObject(put, object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult> {
                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult) {
                        val eTag = result.eTag
                        val serverCallbackReturnBody = result.serverCallbackReturnBody
                        Log.e("PutObject", "UploadSuccess$eTag--$serverCallbackReturnBody")
                        if (callback != null && result.statusCode == 200) {
                            callback.onUpload(ALIYUN_BUCKET_URL + put.objectKey)
                        }
                    }

                    override fun onFailure(
                        request: PutObjectRequest?,
                        clientExcepion: ClientException,
                        serviceException: ServiceException
                    ) {
                        printError(clientExcepion, serviceException)
                        callback?.onError(serviceException.rawMessage)
                    }
                })
        }

        private fun printError(clientExcepion: ClientException?, serviceException: ServiceException?) {
            // 请求异常
            clientExcepion?.printStackTrace()
            if (serviceException != null) {
                // 服务异常
                Log.e("ErrorCode", serviceException.errorCode)
                Log.e("RequestId", serviceException.requestId)
                Log.e("HostId", serviceException.hostId)
                Log.e("RawMessage", serviceException.rawMessage)
            }
        }
    }

    fun download(url: String?, filePath: String?, callback: DownloadCallback?) {
        // 构造下载文件请求
        val get = GetObjectRequest(BUCKET_NAME, url)
        val task: OSSAsyncTask<*> =
            oss!!.asyncGetObject(get, object : OSSCompletedCallback<GetObjectRequest?, GetObjectResult> {
                override fun onSuccess(request: GetObjectRequest?, result: GetObjectResult) {
                    Log.d("Content-Length", "" + result.contentLength)
                    //FileUtil.SaveFile(filePath, result.getObjectContent());
                    callback?.onDownloadSuccess(filePath)
                }

                override fun onFailure(
                    request: GetObjectRequest?,
                    clientExcepion: ClientException,
                    serviceException: ServiceException
                ) {
                    // 请求异常
                    printError(clientExcepion, serviceException)
                    callback?.onError(serviceException.rawMessage)
                }
            })
    }

    interface UploadCallback {
        fun onUpload(result: String?)
        fun onError(error: String?)
    }

    interface DownloadCallback {
        fun onDownloadSuccess(fileUrl: String?)
        fun onError(error: String?)
    }
}
