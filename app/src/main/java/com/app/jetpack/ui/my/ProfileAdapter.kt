package com.app.jetpack.ui.my

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.paging.ItemKeyedDataSource
import com.app.jetpack.R
import com.app.jetpack.core.TAB_TYPE_COMMENT
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.base.MutableItemKeyedDataSource
import com.app.jetpack.ui.home.FeedAdapter
import com.app.jetpack.ui.home.InteractionHelper
import com.app.lib_common.util.TimeUtil

open class ProfileAdapter(activity: FragmentActivity, category: String) : FeedAdapter(activity, category) {
    override fun getDataItemViewType(position: Int): Int {
        return if (TAB_TYPE_COMMENT == category) {
            R.layout.layout_feed_type_comment
        } else {
            super.getDataItemViewType(position)
        }
    }

    override fun onBindDataViewHolder(holder: ViewHolder, position: Int) {
        super.onBindDataViewHolder(holder, position)
        val feed = getItem(position) ?: return
        holder.itemView.findViewById<View>(R.id.feed_delete)?.apply {
            isVisible = true
            setOnClickListener {
                if (TAB_TYPE_COMMENT == category) {
                    InteractionHelper.deleteFeedComment(context, feed.itemId, feed.topComment?.commentId ?: 0L)
                        .observe(context as LifecycleOwner) {
                            refreshList(feed)
                        }
                } else {
                    InteractionHelper.deleteFeed(context as FragmentActivity, feed.itemId)
                        .observe(context as LifecycleOwner) {
                            refreshList(feed)
                        }
                }
            }
        }
        holder.itemView.findViewById<TextView>(R.id.create_time)?.apply {
            isVisible = true
            text = TimeUtil.calculate(feed.createTime)
        }
        if (TAB_TYPE_COMMENT == category) {
            holder.itemView.findViewById<View>(R.id.diss)?.isVisible = false
        }
    }

    private fun refreshList(delete: Feed) {
        val dataSource =
            object : MutableItemKeyedDataSource<Int, Feed>(currentList?.dataSource as ItemKeyedDataSource<Int, Feed>) {
                override fun getKey(item: Feed): Int {
                    return item.id
                }
            }
        currentList?.forEach {
            if (it != delete) {
                dataSource.data.add(it)
            }
        }
        submitList(dataSource.buildNewPagedList(currentList?.config!!))
    }
}
