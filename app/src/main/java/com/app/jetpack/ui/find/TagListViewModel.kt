package com.app.jetpack.ui.find

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.alibaba.fastjson2.TypeReference
import com.app.jetpack.model.TagList
import com.app.jetpack.ui.base.BasePagedViewModel
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.ext.safeAs
import com.app.lib_network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class TagListViewModel : BasePagedViewModel<Long, TagList>() {
    override val initialLoadKey: Long = 0L

    private var mTagType = ""
    private var mOffset = 0
    private val mLoadAfter = AtomicBoolean()
    val switchTabLiveData = MutableLiveData<Boolean>()

    fun setTagType(tagType: String?) {
        mTagType = tagType.orEmpty()
    }

    override fun createDataSource(): DataSource<Long, TagList> {
        return TagDataSource()
    }

    inner class TagDataSource : ItemKeyedDataSource<Long, TagList>() {
        override fun getKey(item: TagList): Long {
            return item.tagId
        }

        override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<TagList>) {
            loadData(params.key, callback)
        }

        override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<TagList>) {
            callback.onResult(emptyList())
        }

        override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<TagList>) {
            loadData(params.requestedInitialKey ?: 0L, callback)
        }

        fun loadData(key: Long, callback: LoadCallback<TagList>) {
            if (key > 0) {
                mLoadAfter.set(true)
            }
            val response = ApiService.get<List<TagList>>("/tag/queryTagList")
                .addParam("userId", UserManager.getUserId())
                .addParam("tagId", key)
                .addParam("tagType", mTagType)
                .addParam("pageCount", 10)
                .addParam("offset", mOffset)
                .responseType(object : TypeReference<ArrayList<TagList>>() {}.type)
                .execute()
            val result = response.body ?: emptyList()
            callback.onResult(result)
            if (key > 0) {
                mLoadAfter.set(false)
                getBoundaryPagedData().safeAs<MutableLiveData<Boolean>>()?.postValue(result.isNotEmpty())
            }
            mOffset += result.size
        }
    }

    fun loadData(tagId: Long, callback: ItemKeyedDataSource.LoadCallback<TagList>) {
        if (tagId <= 0 || mLoadAfter.get()) {
            callback.onResult(emptyList())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            getDataSource().safeAs<TagDataSource>()?.loadData(tagId, callback)
        }
    }
}