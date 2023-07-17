package com.app.jetpack.ui.publish

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.app.jetpack.R
import com.app.jetpack.core.PATH_MAIN_PUBLISH
import com.app.jetpack.core.RESULT_FILE_HEIGHT
import com.app.jetpack.core.RESULT_FILE_PATH
import com.app.jetpack.core.RESULT_FILE_TYPE
import com.app.jetpack.core.RESULT_FILE_WIDTH
import com.app.jetpack.databinding.ActivityCaptureBinding
import com.app.jetpack.view.RecordView
import com.app.lib_common.base.BaseActivity
import com.app.lib_common.util.FileUtil
import com.app.lib_nav_annotation.annotation.ActivityDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@ActivityDestination(PATH_MAIN_PUBLISH)
class CaptureActivity : BaseActivity(), RecordView.OnRecordListener {

    companion object {
        private const val PERMISSION_CODE = 1000
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private lateinit var mBinding: ActivityCaptureBinding

    private val mDeniedPermission = ArrayList<String>()

    private lateinit var mImageCapture: ImageCapture
    private lateinit var mVideoCapture: VideoCapture

    private var mIsPicture = false
    private var mOutputFilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Capture_Theme)
        super.onCreate(savedInstanceState)
        mBinding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.recordView.setOnRecordListener(this)
        if (allPermissionsGranted()) {
            bindCameraX()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_CODE)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISSION_CODE != requestCode) {
            return
        }
        mDeniedPermission.clear()
        permissions.forEachIndexed { i, p ->
            val result = grantResults[i]
            if (PackageManager.PERMISSION_GRANTED != result) {
                mDeniedPermission.add(p)
            }
        }
        if (mDeniedPermission.isEmpty()) {
            bindCameraX()
            return
        }
        AlertDialog.Builder(this)
            .setMessage(R.string.capture_permission_message)
            .setNegativeButton(R.string.capture_permission_no) { dialog, _ ->
                dialog.dismiss()
                finish()
            }.setPositiveButton(R.string.capture_permission_ok) { _, _ ->
                ActivityCompat.requestPermissions(this, mDeniedPermission.toTypedArray(), PERMISSION_CODE)
            }.create()
            .show()
    }

    @SuppressLint("RestrictedApi")
    private fun bindCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(mBinding.previewView.surfaceProvider)
            }
            mVideoCapture = VideoCapture.Builder().build()
            mImageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this@CaptureActivity,
                    cameraSelector,
                    preview,
                    mImageCapture,
                    mVideoCapture
                )
            }
        }, ActivityCompat.getMainExecutor(this))
    }

    override fun onClick() {
        mIsPicture = true
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        mImageCapture.takePicture(
            outputOptions,
            ActivityCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onFileSaved(outputFileResults.savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    showErrorToast(exception.message.orEmpty())
                }
            })
    }

    @SuppressLint("RestrictedApi")
    override fun onLongClick() {
        mIsPicture = false
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }
        val mediaStoreOutputOptions = VideoCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mVideoCapture.startRecording(
            mediaStoreOutputOptions,
            ActivityCompat.getMainExecutor(this),
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    onFileSaved(outputFileResults.savedUri)
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    showErrorToast(message)
                }

            })
    }

    @SuppressLint("RestrictedApi")
    override fun onFinish() {
        mVideoCapture.stopRecording()
    }

    private fun onFileSaved(uri: Uri?) {
        mOutputFilePath = FileUtil.getPathFromUri(this, uri).orEmpty()
        if (mOutputFilePath.isEmpty()) {
            return
        }
        PreviewActivity.startActivityForResult(this, mOutputFilePath, !mIsPicture, true)
    }

    private fun showErrorToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@CaptureActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
            val intent = Intent().apply {
                putExtra(RESULT_FILE_PATH, mOutputFilePath)
                putExtra(RESULT_FILE_WIDTH, 1280)
                putExtra(RESULT_FILE_HEIGHT, 720)
                putExtra(RESULT_FILE_TYPE, mIsPicture)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}