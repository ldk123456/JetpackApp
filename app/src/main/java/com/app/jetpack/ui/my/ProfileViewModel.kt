package com.app.jetpack.ui.my

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.Feed
import com.app.jetpack.ui.base.BasePagedViewModel
import com.app.jetpack.ui.login.UserManager
import com.app.lib_network.ApiService

class ProfileViewModel : BasePagedViewModel<Int, Feed>() {
    override val initialLoadKey: Int = 0

    private var mProfileType = ""

    fun setProfileType(profileType: String) {
        mProfileType = profileType
    }

    override fun createDataSource(): DataSource<Int, Feed> {
        return ProfileDataSource()
    }

    inner class ProfileDataSource : ItemKeyedDataSource<Int, Feed>() {
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

        private fun loadData(key: Int, callback: LoadCallback<Feed>) {
            ApiService.get<List<Feed>>("/feeds/queryProfileFeeds")
                .addParam("userId", UserManager.getUserId())
                .addParam("profileType", mProfileType)
                .addParam("inId", key)
                .responseType(object : TypeReference<List<Feed>>() {}.type)
                .execute()
                .let {
                    callback.onResult(it.body ?: emptyList())
                }
        }
    }
}