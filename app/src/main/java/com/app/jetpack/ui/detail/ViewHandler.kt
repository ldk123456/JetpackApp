package com.app.jetpack.ui.detail

import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.databinding.LayoutFeedDetailBottomInateractionBinding
import com.app.jetpack.model.Feed
import com.app.lib_common.view.EmptyView

abstract class ViewHandler(protected val activity: FragmentActivity) {
    protected lateinit var mFeed: Feed
    protected lateinit var mRecyclerView: RecyclerView
    protected lateinit var mCommentAdapter: FeedCommentAdapter
    protected lateinit var mInteractionBinding: LayoutFeedDetailBottomInateractionBinding

    protected val mViewModel = ViewModelProvider(activity)[FeedDetailViewModel::class.java]

    @CallSuper
    open fun bindInitData(feed: Feed) {
        mFeed = feed
        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            mCommentAdapter = FeedCommentAdapter(activity)
            adapter = mCommentAdapter
        }

        mViewModel.setItemId(feed.itemId)
        mViewModel.getPagedData().observe(activity) {
            mCommentAdapter.submitList(it)
            handleEmpty(it.isNotEmpty())
        }
        handleEmpty(false)
    }

    private var mEmptyView: EmptyView? = null

    fun handleEmpty(hasData: Boolean) {
        if (hasData) {
            mCommentAdapter.removeHeaderView(mEmptyView)
        } else {
            if (mEmptyView == null) {
                mEmptyView = EmptyView(activity).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setTitle(activity.getString(R.string.feed_comment_empty))
                }
            }
            mCommentAdapter.addHeaderView(mEmptyView)
        }
    }
}