package com.app.jetpack.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.app.jetpack.R
import com.app.jetpack.databinding.LayoutPlayerViewBinding
import com.app.jetpack.player.IPlayTarget
import com.app.jetpack.player.PageListPlayerManager
import com.app.lib_common.ext.getScreenWidth
import com.app.lib_common.ext.safeAs
import com.app.lib_common.ext.setVisible
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView

open class ListPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IPlayTarget,
    PlayerControlView.VisibilityListener, Player.Listener {

    protected val binding = LayoutPlayerViewBinding.inflate(LayoutInflater.from(context), this)

    protected var mCategory: String = ""
    protected var mVideoUrl: String = ""

    private var mIsPlaying = false

    protected var mWidthPx = 0
    protected var mHeightPx = 0

    init {
        binding.ivPlay.setOnClickListener {
            if (isPlaying()) {
                inActive()
            } else {
                onActive()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        PageListPlayerManager.get(mCategory).playerControlView?.show()
        return true
    }

    fun bindData(category: String?, coverUrl: String?, videoUrl: String?, width: Int, height: Int) {
        if (coverUrl.isNullOrEmpty() || videoUrl.isNullOrEmpty()) {
            return
        }
        mCategory = category.orEmpty()
        mVideoUrl = videoUrl

        mWidthPx = width
        mHeightPx = height

        binding.ivCover.setImageUrl(coverUrl)
        if (width < height) {
            binding.ivBlurBg.setBlurImageUrl(coverUrl, 10)
            binding.ivBlurBg.setVisible(true)
        } else {
            binding.ivBlurBg.setVisible(false)
        }
        setSize(width, height)
    }

    protected open fun setSize(
        width: Int, height: Int,
        maxWidth: Int = getScreenWidth(),
        maxHeight: Int = getScreenWidth()
    ) {
        val layoutWidth = maxWidth
        val layoutHeight: Int
        val coverWidth: Int
        val coverHeight: Int
        if (width >= height) {
            coverWidth = maxWidth
            coverHeight = ((height / (width * 1.0f / maxWidth)).toInt())
            layoutHeight = coverHeight
        } else {
            coverHeight = maxHeight
            layoutHeight = maxHeight
            coverWidth = ((width / (height * 1.0f / maxHeight)).toInt())
        }

        layoutParams.apply {
            this.width = layoutWidth
            this.height = layoutHeight
        }
        binding.ivBlurBg.layoutParams.apply {
            this.width = layoutWidth
            this.height = layoutHeight
        }
        binding.ivCover.layoutParams.apply {
            this.width = coverWidth
            this.height = coverHeight
        }
    }

    override fun getOwner(): ViewGroup {
        return this
    }

    override fun onActive() {
        val player = PageListPlayerManager.get(mCategory)
        if (player.playerView == null) {
            return
        }
        player.switchPlayerView(player.playerView)
        player.playerView?.parent.let {
            if (it != this) {
                it.safeAs<ViewGroup>()?.removeView(player.playerView)
                addView(player.playerView, 1, binding.ivCover.layoutParams)
            }
        }
        player.playerControlView?.parent.let {
            if (it != this) {
                it.safeAs<ViewGroup>()?.removeView(player.playerControlView)
                val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.gravity = Gravity.BOTTOM
                addView(player.playerControlView, params)
            }
        }
        if (mVideoUrl != player.playUrl) {
            val mediaSource = PageListPlayerManager.createMediaSource(mVideoUrl)
            player.exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE
                player.playUrl = mVideoUrl
            }
        } else {
            onPlayerStateChanged(true, Player.STATE_READY)
        }
        player.playerControlView?.addVisibilityListener(this)
        player.exoPlayer?.addListener(this)
        player.playerControlView?.show()
        player.exoPlayer?.playWhenReady = true
    }

    override fun inActive() {
        PageListPlayerManager.get(mCategory).apply {
            exoPlayer?.playWhenReady = false
            playerControlView?.removeVisibilityListener(this@ListPlayerView)
            exoPlayer?.removeListener(this@ListPlayerView)
            binding.ivCover.isVisible = true
            binding.ivPlay.isVisible = true
            binding.ivPlay.setImageResource(R.drawable.icon_video_play)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mIsPlaying = false
        binding.pbBuffer.isVisible = false
        binding.ivCover.isVisible = true
        binding.ivPlay.isVisible = true
        binding.ivPlay.setImageResource(R.drawable.icon_video_play)
    }

    override fun isPlaying(): Boolean {
        return mIsPlaying
    }

    fun getPlayerController() = PageListPlayerManager.get(mCategory).playerControlView

    override fun onVisibilityChange(visibility: Int) {
        binding.ivPlay.visibility = visibility
        binding.ivPlay.setImageResource(if (isPlaying()) R.drawable.icon_video_pause else R.drawable.icon_video_play)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        val player = PageListPlayerManager.get(mCategory).exoPlayer ?: return
        if (Player.STATE_READY == playbackState && player.bufferedPosition != 0L && playWhenReady) {
            binding.ivCover.isVisible = false
            binding.pbBuffer.isVisible = false
        } else if (Player.STATE_BUFFERING == playbackState) {
            binding.pbBuffer.isVisible = true
        }
        mIsPlaying = Player.STATE_READY == playbackState && player.bufferedPosition != 0L && playWhenReady
        binding.ivPlay.setImageResource(if (isPlaying()) R.drawable.icon_video_pause else R.drawable.icon_video_play)
    }
}