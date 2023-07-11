package com.app.jetpack.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.base.BasePagedViewModel
import com.app.jetpack.ui.base.MutableDataSource
import com.app.jetpack.ui.login.UserManager
import com.app.lib_network.ApiService
import com.app.lib_network.core.CacheStrategy
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class HomeViewModel : BasePagedViewModel<Feed>() {
    @Volatile
    private var mWithCache = true
    private val mCacheLiveData = MutableLiveData<PagedList<Feed>>()
    private val mLoadAfter = AtomicBoolean(false)

    private var mFeedType: String = ""

    fun setFeedType(feedType: String) {
        this.mFeedType = feedType
    }

    fun getCacheLiveData() = mCacheLiveData

    override fun createDataSource(): DataSource<out Any, Feed> {
        return FeedDataSource()
    }

    inner class FeedDataSource : ItemKeyedDataSource<Int, Feed>() {
        override fun getKey(item: Feed): Int {
            return item.id
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            loadData(params.key, callback)
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            callback.onResult(listOf())
        }

        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Feed>) {
            loadData(0, callback)
            mWithCache = false
        }
    }

    private fun loadData(key: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
        if (key > 0) {
            mLoadAfter.set(true)
        }
        val request = ApiService.get<ArrayList<Feed>>("/feeds/queryHotFeedsList")
            .addParam("feedType", mFeedType)
            .addParam("userId", UserManager.getUserId())
            .addParam("feedId", key)
            .addParam("pageCount", 10)
            .responseType(object : TypeReference<ArrayList<Feed>>() {}.type)
        if (mWithCache) {
            request.cacheStrategy(CacheStrategy.CACHE_ONLY)
            request.execute(object : JsonCallback<ArrayList<Feed>> {
                override fun onCacheSuccess(response: ApiResponse<ArrayList<Feed>>) {
                    Log.e("loadData", "onCacheSuccess: ")
                    val dataSource = MutableDataSource<Int, Feed>()
                    dataSource.data.addAll(response.body ?: listOf())
                    mCacheLiveData.postValue(dataSource.buildNewPagedList(config))
                }
            })
        }
        val netRequest = if (mWithCache) request.clone() else request
        netRequest.cacheStrategy(if (key == 0) CacheStrategy.NET_CACHE else CacheStrategy.NET_ONLY)
        val response = netRequest.execute()
        callback.onResult(response.body ?: listOf())
        if (key > 0) {
            getBoundaryPagedData().postValue(!response.body.isNullOrEmpty())
            mLoadAfter.set(false)
        }
        Log.e("loadData", "key: $key")
    }

    fun loadAfter(key: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
        if (mLoadAfter.get()) {
            callback.onResult(listOf())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            loadData(key, callback)
        }
    }
}