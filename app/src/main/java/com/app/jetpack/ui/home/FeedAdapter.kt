package com.app.jetpack.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.databinding.LayoutFeedTypeImageBinding
import com.app.jetpack.databinding.LayoutFeedTypeVideoBinding
import com.app.jetpack.model.Feed

class FeedAdapter(
    private val context: Context,
    private val category: String
) : PagedListAdapter<Feed, FeedAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
    }) {


    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.itemType ?: Feed.TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == Feed.TYPE_IMAGE) {
            LayoutFeedTypeImageBinding.inflate(inflater, parent, false)
        } else {
            LayoutFeedTypeVideoBinding.inflate(inflater, parent, false)
        }.let {
            ViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    inner class ViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(feed: Feed?) {
            feed ?: return
            if (binding is LayoutFeedTypeImageBinding) {
                binding.feed = feed
                binding.ivFeed.bindData(feed.cover, feed.width, feed.height, 16)
            } else if (binding is LayoutFeedTypeVideoBinding) {
                binding.feed = feed
                binding.listPlayerView.bindData(category, feed.cover, feed.url, feed.width, feed.height)
            }
        }
    }
}