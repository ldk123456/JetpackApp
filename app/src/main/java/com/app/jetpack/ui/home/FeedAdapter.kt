package com.app.jetpack.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.core.DATA_FROM_INTERACTION
import com.app.jetpack.databinding.LayoutFeedTypeImageBinding
import com.app.jetpack.databinding.LayoutFeedTypeVideoBinding
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.detail.FeedDetailActivity
import com.app.jetpack.view.ListPlayerView
import com.app.lib_common.app.LiveDataBus
import com.app.lib_common.base.BasePagedListAdapter

open class FeedAdapter(
    private val context: Context,
    private val category: String
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
        return getItem(position)?.itemType ?: Feed.TYPE_IMAGE
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == Feed.TYPE_IMAGE) {
            LayoutFeedTypeImageBinding.inflate(inflater, parent, false)
        } else {
            LayoutFeedTypeVideoBinding.inflate(inflater, parent, false)
        }.let {
            ViewHolder(it)
        }
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
            if (binding is LayoutFeedTypeImageBinding) {
                feedImage = binding.ivFeed
                binding.feed = feed
                binding.ivFeed.bindData(feed.cover, feed.width, feed.height, 16)
                binding.interactionLayout.lifecycleOwner = context as? LifecycleOwner
            } else if (binding is LayoutFeedTypeVideoBinding) {
                binding.feed = feed
                binding.listPlayerView.bindData(category, feed.cover, feed.url, feed.width, feed.height)
                binding.interactionLayout.lifecycleOwner = context as? LifecycleOwner
                listPlayerView = binding.listPlayerView
            }
        }

        fun isVideoItem() = binding is LayoutFeedTypeVideoBinding
    }
}