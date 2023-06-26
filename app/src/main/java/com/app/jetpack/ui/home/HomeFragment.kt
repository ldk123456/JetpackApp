package com.app.jetpack.ui.home

import android.os.Bundle
import android.view.View
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.core.KEY_FEED_TYPE
import com.app.jetpack.core.PATH_MAIN_HOME
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.base.BaseListFragment
import com.app.jetpack.ui.base.MutableDataSource
import com.app.lib_nav_annotation.annotation.FragmentDestination
import com.scwang.smart.refresh.layout.api.RefreshLayout


@FragmentDestination(PATH_MAIN_HOME, asStarter = true)
class HomeFragment : BaseListFragment<Feed, HomeViewModel>() {

    private val mFeedType: String by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getString(KEY_FEED_TYPE) ?: "all"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel?.getCacheLiveData()?.observe(viewLifecycleOwner) {
            submitList(it)
        }
    }

    override fun getAdapter(): PagedListAdapter<Feed, out RecyclerView.ViewHolder> {
        return FeedAdapter(requireContext(), mFeedType)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel?.getDataSource()?.invalidate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        val feed = mAdapter.currentList?.last() ?: return
        mViewModel?.loadAfter(feed.id, object : ItemKeyedDataSource.LoadCallback<Feed>() {
            override fun onResult(data: List<Feed>) {
                val config = mAdapter.currentList?.config
                if (config != null && data.isNotEmpty()) {
                    val dataSource = MutableDataSource<Int, Feed>()
                    dataSource.data.addAll(data)
                    submitList(dataSource.buildNewPagedList(config))
                }
            }
        })
    }
}