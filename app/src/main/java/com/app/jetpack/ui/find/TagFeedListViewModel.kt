package com.app.jetpack.ui.find

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.base.BasePagedViewModel
import com.app.jetpack.ui.login.UserManager
import com.app.lib_network.ApiService

class TagFeedListViewModel : BasePagedViewModel<Int, Feed>() {
    override val initialLoadKey: Int = 0

    private var mFeedType = ""

    fun setFeedType(feedType: String?) {
        mFeedType = feedType.orEmpty()
    }

    override fun createDataSource(): DataSource<Int, Feed> {
        return TagFeedDataSource()
    }

    inner class TagFeedDataSource : ItemKeyedDataSource<Int, Feed>() {
        override fun getKey(item: Feed): Int {
            return item.id
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            loadData(params.key, callback)
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            callback.onResult(emptyList())
        }

        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Feed>) {
            loadData(params.requestedInitialKey ?: 0, callback)
        }

        private fun loadData(feedId: Int, callback: LoadCallback<Feed>) {
            val response = ApiService.get<List<Feed>>("/feeds/queryHotFeedsList")
                .addParam("userId", UserManager.getUserId())
                .addParam("pageCount", 10)
                .addParam("feedType", mFeedType)
                .addParam("feedId", feedId)
                .responseType(object : TypeReference<ArrayList<Feed>>() {}.type)
                .execute()

            callback.onResult(response.body ?: emptyList());
        }
    }
}