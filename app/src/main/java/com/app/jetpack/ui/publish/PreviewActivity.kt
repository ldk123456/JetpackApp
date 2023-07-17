package com.app.jetpack.ui.publish

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.app.jetpack.R
import com.app.jetpack.core.KEY_IS_VIDEO
import com.app.jetpack.core.KEY_PREVIEW_PATH
import com.app.jetpack.core.KEY_SHOW_ACTION_BTN
import com.app.jetpack.databinding.ActivityPreviewBinding
import com.app.lib_common.base.BaseActivity
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource

class PreviewActivity : BaseActivity(), View.OnClickListener {
    companion object {
        const val REQ_PREVIEW = 1000

        fun startActivityForResult(
            activity: AppCompatActivity,
            previewPath: String,
            isVideo: Boolean,
            showActionBtn: Boolean
        ) {
            val intent = Intent(activity, PreviewActivity::class.java).apply {
                putExtra(KEY_PREVIEW_PATH, previewPath)
                putExtra(KEY_IS_VIDEO, isVideo)
                putExtra(KEY_SHOW_ACTION_BTN, showActionBtn)
            }
            activity.startActivityForResult(intent, REQ_PREVIEW)
        }
    }

    private lateinit var mBinding: ActivityPreviewBinding

    private val mPreviewPath by lazy {
        intent.getStringExtra(KEY_PREVIEW_PATH).orEmpty()
    }
    private val mIsVideo by lazy {
        intent.getBooleanExtra(KEY_IS_VIDEO, false)
    }
    private val mShowActionBtn by lazy {
        intent.getBooleanExtra(KEY_SHOW_ACTION_BTN, false)
    }

    private var mPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        if (mPreviewPath.isEmpty()) {
            finish()
            return
        }
        mBinding.actionOk.isVisible = mShowActionBtn
        mBinding.actionOk.setOnClickListener(this)
        mBinding.actionClose.setOnClickListener(this)
        if (mIsVideo) {
            previewVideo()
        } else {
            previewImage()
        }
    }

    private fun previewImage() {
        mBinding.photoView.apply {
            Glide.with(this@PreviewActivity).load(mPreviewPath).into(this)
            isVisible = true
        }
    }

    private fun previewVideo() {
        mPlayer = ExoPlayer.Builder(this).build()
        mBinding.playerView.player = mPlayer

        val dataSpec = DataSpec(Uri.parse(mPreviewPath))
        val dataSource = FileDataSource()
        runCatching {
            dataSource.open(dataSpec)
            val uri = dataSource.uri
            val source = ProgressiveMediaSource.Factory(FileDataSource.Factory())
                .createMediaSource(MediaItem.fromUri(uri!!))
            mPlayer?.setMediaSource(source)
            mPlayer?.prepare()
            mPlayer?.playWhenReady = true
            mBinding.playerView.isVisible = true
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.action_close -> {
                finish()
            }

            R.id.action_ok -> {
                setResult(RESULT_OK, Intent())
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mPlayer?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        mPlayer?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer?.apply {
            playWhenReady = false
            stop()
            clearMediaItems()
            release()
        }
    }
}