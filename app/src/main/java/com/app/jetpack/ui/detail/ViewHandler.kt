package com.app.jetpack.ui.detail

import android.content.Intent
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ItemKeyedDataSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.databinding.LayoutFeedDetailBottomInteractionBinding
import com.app.jetpack.model.Comment
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.base.MutableItemKeyedDataSource
import com.app.lib_common.view.EmptyView

abstract class ViewHandler(protected val activity: FragmentActivity) {
    protected lateinit var mFeed: Feed
    protected lateinit var mRecyclerView: RecyclerView
    protected lateinit var mCommentAdapter: FeedCommentAdapter
    protected lateinit var mInteractionBinding: LayoutFeedDetailBottomInteractionBinding

    protected val mViewModel = ViewModelProvider(activity)[FeedDetailViewModel::class.java]

    private var mCommentDialog: CommentDialog? = null

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
        mInteractionBinding.inputView.setOnClickListener {
            showCommentDialog()
        }
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

    private fun showCommentDialog() {
        val dialog = mCommentDialog ?: run {
            val commentDialog = CommentDialog.newInstance(mFeed.itemId)
            mCommentDialog = commentDialog
            commentDialog
        }
        dialog.setOnCommentAddListener(object : CommentDialog.OnCommentAddListener {
            override fun onCommentAdd(comment: Comment) {
                val dataSource = object : MutableItemKeyedDataSource<Int, Comment>(
                    mViewModel.getDataSource() as ItemKeyedDataSource<Int, Comment>
                ) {
                    override fun getKey(item: Comment): Int {
                        return comment.id
                    }
                }
                dataSource.data.add(comment)
                dataSource.data.addAll(mCommentAdapter.currentList ?: emptyList())
                mCommentAdapter.submitList(dataSource.buildNewPagedList(mCommentAdapter.currentList?.config!!))
                mCommentDialog = null
            }
        })
        dialog.show(activity.supportFragmentManager, CommentDialog.TAG)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCommentDialog?.takeIf { it.isAdded }?.onActivityResult(requestCode, resultCode, data)
    }

    open fun onResume() {}
    open fun onBackPressed() {}
    open fun onPause() {}
}