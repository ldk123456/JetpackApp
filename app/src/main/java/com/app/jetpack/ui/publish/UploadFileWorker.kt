package com.app.jetpack.ui.publish

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.jetpack.core.KEY_FILE_PATH
import com.app.jetpack.core.KEY_FILE_URL
import com.app.lib_common.util.FileUploadManager

class UploadFileWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val filePath = inputData.getString(KEY_FILE_PATH)
        val fileUrl = FileUploadManager.upload(filePath.orEmpty())
        return if (fileUrl.isNullOrEmpty()) {
            Result.failure()
        } else {
            val outputData = Data.Builder()
                .putString(KEY_FILE_URL, fileUrl)
                .build()
            Result.success(outputData)
        }
    }
}