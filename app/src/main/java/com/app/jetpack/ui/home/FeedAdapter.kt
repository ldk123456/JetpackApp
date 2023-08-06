package com.app.jetpack.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.core.DATA_FROM_INTERACTION
import com.app.jetpack.databinding.LayoutFeedTopCommentBinding
import com.app.jetpack.databinding.LayoutFeedTypeCommentBinding
import com.app.jetpack.databinding.LayoutFeedTypeImageBinding
import com.app.jetpack.databinding.LayoutFeedTypeVideoBinding
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.detail.FeedDetailActivity
import com.app.jetpack.view.ListPlayerView
import com.app.lib_common.app.LiveDataBus
import com.app.lib_common.base.BasePagedListAdapter

open class FeedAdapter(
    protected val context: Context,
    protected val category: String
) : BasePagedListAdapter<Feed, FeedAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun getDataItemViewType(position: Int): Int {
        return when (getItem(position)?.itemType) {
            Feed.TYPE_IMAGE -> R.layout.layout_feed_type_image
            Feed.TYPE_VIDEO -> R.layout.layout_feed_type_video
            else -> 0
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, parent, false)
        return ViewHolder((binding))
    }

    override fun onBindDataViewHolder(holder: ViewHolder, position: Int) {
        val feed = getItem(position) ?: return
        holder.bindData(feed)
        holder.itemView.setOnClickListener {
            FeedDetailActivity.startActivity(context, feed, category)
            onStartFeedDetailActivity(feed)
            if (mFeedObserver == null) {
                mFeedObserver = FeedObserver()
                LiveDataBus.with<Feed>(DATA_FROM_INTERACTION)
                    .observe(context as LifecycleOwner, mFeedObserver!!)
            }
            mFeedObserver?.feed = feed
        }
    }

    open fun onStartFeedDetailActivity(feed: Feed) {

    }

    private var mFeedObserver: FeedObserver? = null

    private class FeedObserver : Observer<Feed> {
        var feed: Feed? = null
        override fun onChanged(value: Feed) {
            feed?.takeIf { value.id == it.id }?.let {
                it.author = value.author
                it.ugc = value.ugc
                it.notifyChange()
            }
        }
    }

    inner class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        var listPlayerView: ListPlayerView? = null
            private set
        var feedImage: ImageView? = null

        fun bindData(feed: Feed?) {
            feed ?: return
            binding.setVariable(com.app.jetpack.BR.feed, feed)
            when (binding) {
                is LayoutFeedTypeImageBinding -> {
                    feedImage = binding.ivFeed
                    binding.ivFeed.bindData(feed.cover, feed.width, feed.height, 16)
                    binding.interactionLayout.lifecycleOwner = context as? LifecycleOwner
                }

                is LayoutFeedTypeVideoBinding -> {
                    binding.listPlayerView.bindData(category, feed.cover, feed.url, feed.width, feed.height)
                    binding.interactionLayout.lifecycleOwner = context as? LifecycleOwner
                    listPlayerView = binding.listPlayerView
                }

                is LayoutFeedTypeCommentBinding -> {
                    binding.interactionBinding.lifecycleOwner = context as? LifecycleOwner
                }
            }
        }

        fun isVideoItem() = binding is LayoutFeedTypeVideoBinding
    }
}