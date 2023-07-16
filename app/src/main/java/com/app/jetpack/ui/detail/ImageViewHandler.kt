package com.app.jetpack.ui.detail

import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.databinding.ActivityFeedDetailTypeImageBinding
import com.app.jetpack.databinding.LayoutFeedDetailTypeImageHeaderBinding
import com.app.jetpack.model.Feed

class ImageViewHandler(activity: FragmentActivity) : ViewHandler(activity) {

    private val mImageBinding: ActivityFeedDetailTypeImageBinding
    private lateinit var mHeaderBinding: LayoutFeedDetailTypeImageHeaderBinding

    init {
        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image)
        mRecyclerView = mImageBinding.recyclerView
        mInteractionBinding = mImageBinding.interactionLayout
    }

    override fun bindInitData(feed: Feed) {
        super.bindInitData(feed)
        mImageBinding.feed = feed
        mImageBinding.owner = activity
        mImageBinding.ivBack.setOnClickListener { activity.finish() }
        mHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(activity.layoutInflater, mRecyclerView, false)
        mHeaderBinding.feed = feed
        mHeaderBinding.owner = activity
        val leftMargin = if (feed.width > feed.height) 0 else 16
        mHeaderBinding.headerImage.bindData(feed.cover, feed.width, feed.height, leftMargin)
        mCommentAdapter.addHeaderView(mHeaderBinding.root)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visible = mHeaderBinding.root.top <= -mImageBinding.titleLayout.measuredHeight
                mImageBinding.authorInfoLayout.root.isVisible = visible
                mImageBinding.title.isVisible = !visible
            }
        })
        handleEmpty(false)
    }
}