package com.app.jetpack.ui.detail

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.Comment
import com.app.jetpack.ui.base.BasePagedViewModel
import com.app.jetpack.ui.login.UserManager
import com.app.lib_network.ApiService

class FeedDetailViewModel : BasePagedViewModel<Int, Comment>() {
    override val initialLoadKey: Int = 0

    private var mItemId: Long = 0L

    fun setItemId(itemId: Long) {
        mItemId = itemId
    }

    override fun createDataSource(): DataSource<Int, Comment> {
        return CommentDataSource()
    }

    inner class CommentDataSource : ItemKeyedDataSource<Int, Comment>() {
        override fun getKey(item: Comment): Int {
            return item.id
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Comment>) {
            loadData(params.key, params.requestedLoadSize, callback)
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Comment>) {
            callback.onResult(emptyList())
        }

        override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Comment>) {
            loadData(params.requestedInitialKey ?: 0, params.requestedLoadSize, callback)
        }

        private fun loadData(key: Int, loadSize: Int, callback: LoadCallback<Comment>) {
            ApiService.get<List<Comment>>("/comment/queryFeedComments")
                .addParam("id", key)
                .addParam("itemId", mItemId)
                .addParam("userId", UserManager.getUserId())
                .addParam("pageCount", loadSize)
                .responseType(object : TypeReference<ArrayList<Comment>>() {}.type)
                .execute().let {
                    callback.onResult(it.body ?: emptyList())
                }
        }
    }
}