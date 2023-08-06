package com.app.jetpack.ui.detail

import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.app.jetpack.R
import com.app.jetpack.core.KEY_CATEGORY
import com.app.jetpack.databinding.LayoutFeedDetailTypeVideoBinding
import com.app.jetpack.databinding.LayoutFeedDetailTypeVideoHeaderBinding
import com.app.jetpack.model.Feed
import com.app.lib_common.ext.safeAs

class VideoViewHandler(activity: FragmentActivity) : ViewHandler(activity) {
    private val mVideoBinding: LayoutFeedDetailTypeVideoBinding

    private var mCategory = ""
    private var mBackPressed = false

    init {
        mVideoBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_video)
        mInteractionBinding = mVideoBinding.bottomInteraction
        mRecyclerView = mVideoBinding.recyclerView

        mVideoBinding.authorInfo.root.layoutParams.safeAs<CoordinatorLayout.LayoutParams>()?.let {
            it.behavior = ViewAnchorBehavior(R.id.player_view)
        }
        mVideoBinding.playerView.layoutParams.safeAs<CoordinatorLayout.LayoutParams>()
            ?.behavior?.safeAs<ViewZomBehavior>()?.setViewZoomCallback(object : ViewZomBehavior.ViewZoomCallback {
                override fun onDragZoom(height: Int) {
                    val moveUp = height < mVideoBinding.playerView.bottom
                    val fullscreen = if (moveUp) {
                        height >= mVideoBinding.coordinator.bottom - mInteractionBinding.root.height
                    } else {
                        height >= mVideoBinding.coordinator.bottom
                    }
                    setViewAppearance(fullscreen)
                }
            })
        mVideoBinding.actionClose.setOnClickListener { activity.finish() }
    }

    override fun bindInitData(feed: Feed) {
        super.bindInitData(feed)
        mVideoBinding.feed = mFeed
        mVideoBinding.owner = activity
        mCategory = activity.intent?.getStringExtra(KEY_CATEGORY).orEmpty()
        mVideoBinding.actionClose.setOnClickListener { activity.finish() }
        mVideoBinding.playerView.bindData(mCategory, mFeed.cover, mFeed.url, mFeed.width, mFeed.height)
        mVideoBinding.playerView.post {
            val fullscreen = mVideoBinding.playerView.bottom >= mVideoBinding.coordinator.bottom
            setViewAppearance(fullscreen)
        }
        LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(activity), mRecyclerView, false).apply {
            setFeed(mFeed)
            mCommentAdapter.addHeaderView(root)
        }
    }

    private fun setViewAppearance(fullscreen: Boolean) {
        mVideoBinding.fullScreen = fullscreen
        mInteractionBinding.fullScreen = fullscreen
        mVideoBinding.fullscreenAuthorInfo.root.isVisible = fullscreen

        val inputHeight = mInteractionBinding.root.measuredHeight
        val controlViewHeight = mVideoBinding.playerView.getPlayerController()?.measuredHeight ?: 0
        val bottom = mVideoBinding.playerView.getPlayerController()?.measuredHeight ?: 0
        mVideoBinding.playerView.getPlayerController()?.translationY = if (fullscreen) {
            bottom - inputHeight - controlViewHeight
        } else {
            bottom - controlViewHeight
        }.toFloat()
        mInteractionBinding.inputView.setBackgroundResource(
            if (fullscreen) R.drawable.bg_edit_view2
            else R.drawable.bg_edit_view
        )
    }

    override fun onResume() {
        super.onResume()
        mBackPressed = false
        mVideoBinding.playerView.onActive()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mBackPressed = true
        mVideoBinding.playerView.getPlayerController()?.translationY = 0f
    }

    override fun onPause() {
        super.onPause()
        if (!mBackPressed) {
            mVideoBinding.playerView.inActive()
        }
    }
}