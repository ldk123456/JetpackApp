package com.app.jetpack.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

abstract class BasePagedViewModel<K : Any, V : Any> : ViewModel() {

    abstract val initialLoadKey: K

    protected val config = PagedList.Config.Builder()
        .setPageSize(10)
        .setInitialLoadSizeHint(10)
        .build()

    private var mDataSource: DataSource<K, V>? = null
    private val mFactory = object : DataSource.Factory<K, V>() {
        override fun create(): DataSource<K, V> {
            if (mDataSource?.isInvalid != false) {
                mDataSource = createDataSource()
            }
            return mDataSource!!
        }
    }

    private val mBoundaryPagedData = MutableLiveData<Boolean>()
    private val mCallback = object : PagedList.BoundaryCallback<V>() {
        override fun onZeroItemsLoaded() {
            mBoundaryPagedData.postValue(false)
        }

        override fun onItemAtFrontLoaded(itemAtFront: V) {
            mBoundaryPagedData.postValue(true)
        }
    }

    private val mPagedData = LivePagedListBuilder(mFactory, config)
        .setInitialLoadKey(initialLoadKey)
        .setBoundaryCallback(mCallback)
        .build()

    abstract fun createDataSource(): DataSource<K, V>

    fun getPagedData() = mPagedData

    fun getDataSource() = mDataSource

    fun getBoundaryPagedData(): LiveData<Boolean> = mBoundaryPagedData
}