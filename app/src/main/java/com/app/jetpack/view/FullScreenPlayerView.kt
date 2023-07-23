package com.app.jetpack.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.app.jetpack.R
import com.app.jetpack.player.PageListPlayerManager
import com.app.lib_common.ext.getScreenHeight
import com.app.lib_common.ext.getScreenWidth
import com.app.lib_common.ext.safeAs
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView

class FullScreenPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ListPlayerView(context, attrs, defStyleAttr) {
    private val mExoPlayerView: StyledPlayerView =
        LayoutInflater.from(context).inflate(R.layout.layout_exo_player_view, this, false) as StyledPlayerView

    override fun setSize(width: Int, height: Int, maxW: Int, maxH: Int) {
        if (width >= height) {
            super.setSize(width, height, maxW, maxH)
            return
        }
        val maxWidth = getScreenWidth()
        val maxHeight = getScreenHeight()
        val params = layoutParams?.apply {
            this.width = maxWidth
            this.height = maxHeight
        }
        layoutParams = params
        binding.ivCover.layoutParams.safeAs<LayoutParams>()?.apply {
            this.width = (width / (height * 1.0f / maxHeight)).toInt()
            this.height = maxHeight
            this.gravity = Gravity.CENTER
        }
        binding.ivCover.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (params != null && mWidthPx < mHeightPx) {
            val height = params.height
            val coverParams = binding.ivCover.layoutParams.apply {
                this.width = (mWidthPx / (mHeightPx * 1.0f / height)).toInt()
                this.height = height
            }
            binding.ivCover.layoutParams = coverParams
            val playerParams = mExoPlayerView.layoutParams
            if (playerParams.width != -1 && params.height != -1) {
                mExoPlayerView.scaleX = coverParams.width * 1.0f / playerParams.width
                mExoPlayerView.scaleY = coverParams.height * 1.0f / playerParams.height
            }
        }
        super.setLayoutParams(params)
    }

    override fun onActive() {
        val pageListPlayer = PageListPlayerManager.get(mCategory)
        val controlView = pageListPlayer.playerControlView
        val exoPlayer = pageListPlayer.exoPlayer
        pageListPlayer.switchPlayerView(mExoPlayerView)
        mExoPlayerView.parent.let {
            if (it != this) {
                it.safeAs<ViewGroup>()?.removeView(mExoPlayerView)
                addView(mExoPlayerView, 1, binding.ivCover.layoutParams)
            }
        }
        controlView?.parent.let {
            if (it != this) {
                it.safeAs<ViewGroup>()?.removeView(controlView)
                val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.gravity = Gravity.BOTTOM
                addView(controlView, params)
            }
        }
        if (mVideoUrl != pageListPlayer.playUrl) {
            val mediaSource = PageListPlayerManager.createMediaSource(mVideoUrl)
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE
                pageListPlayer.playUrl = mVideoUrl
            }
        } else {
            onPlayerStateChanged(true, Player.STATE_READY)
        }
        controlView?.addVisibilityListener(this)
        exoPlayer?.addListener(this)
        controlView?.show()
        exoPlayer?.playWhenReady = true
    }

    override fun inActive() {
        super.inActive()
        mExoPlayerView.player = null
        PageListPlayerManager.get(mCategory).switchPlayerView()
    }
}