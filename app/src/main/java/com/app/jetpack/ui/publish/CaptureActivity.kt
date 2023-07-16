package com.app.jetpack.ui.publish

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.camera.core.VideoCapture
import androidx.camera.core.VideoCaptureConfig
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.app.jetpack.R
import com.app.jetpack.core.PATH_MAIN_PUBLISH
import com.app.jetpack.databinding.ActivityCaptureBinding
import com.app.jetpack.view.RecordView
import com.app.lib_common.base.BaseActivity
import com.app.lib_common.ext.safeAs
import com.app.lib_nav_annotation.annotation.ActivityDestination
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@ActivityDestination(PATH_MAIN_PUBLISH)
class CaptureActivity : BaseActivity(), RecordView.OnRecordListener {

    companion object {
        private const val PERMISSION_CODE = 1000
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private lateinit var mBinding: ActivityCaptureBinding

    private val mDeniedPermission = ArrayList<String>()

    private val mLensFacing = CameraX.LensFacing.BACK
    private val mRotation = Surface.ROTATION_0
    private val mResolution = Size(1280, 720)
    private val mRational = Rational(9, 16)

    private lateinit var mPreview: Preview
    private lateinit var mImageCapture: ImageCapture
    private lateinit var mVideoCapture: VideoCapture

    private var mIsPicture = false
    private var mOutputFilePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Capture_Theme)
        super.onCreate(savedInstanceState)
        mBinding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE)
        mBinding.recordView.setOnRecordListener(this)
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
        mPreview = Preview(
            PreviewConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetRotation(mRotation)
                .setTargetResolution(mResolution)
                .setTargetAspectRatio(mRational)
                .build()
        )
        mImageCapture = ImageCapture(
            ImageCaptureConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetRotation(mRotation)
                .setTargetResolution(mResolution)
                .setTargetAspectRatio(mRational)
                .build()
        )
        mVideoCapture = VideoCapture(
            VideoCaptureConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetRotation(mRotation)
                .setTargetResolution(mResolution)
                .setTargetAspectRatio(mRational)
                .setVideoFrameRate(25)
                .setBitRate(3 * 1024 * 1024)
                .build()
        )
        mPreview.setOnPreviewOutputUpdateListener {
            mBinding.textureView.let { textureView ->
                textureView.parent.safeAs<ViewGroup>()?.apply {
                    removeView(textureView)
                    addView(textureView, 0)
                }
                textureView.setSurfaceTexture(it.surfaceTexture)
            }
        }
    }

    override fun onClick() {
        mIsPicture = true
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "${System.currentTimeMillis()}.jpeg"
        )
        mImageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {
            override fun onImageSaved(file: File) {
                onFileSaved(file)
            }

            override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                showErrorToast(message)
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun onLongClick() {
        mIsPicture = false
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "${System.currentTimeMillis()}.mp4"
        )
        mVideoCapture.startRecording(file, object : VideoCapture.OnVideoSavedListener {
            override fun onVideoSaved(file: File?) {
                file?.let {
                    onFileSaved(it)
                } ?: run {
                    showErrorToast("拍摄失败。")
                }
            }

            override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
                showErrorToast(message ?: "拍摄失败！")
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun onFinish() {
        mVideoCapture.stopRecording()
    }

    private fun onFileSaved(file: File) {
        mOutputFilePath = file.absolutePath
        val mimeType = if (mIsPicture) MimeTypes.IMAGE_JPEG else MimeTypes.VIDEO_MP4
        MediaScannerConnection.scanFile(this, arrayOf(mOutputFilePath), arrayOf(mimeType), null)
    }

    private fun showErrorToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@CaptureActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}