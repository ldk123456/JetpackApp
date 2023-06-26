package com.app.jetpack.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

abstract class BasePagedViewModel<T : Any> : ViewModel() {
    protected val config = PagedList.Config.Builder()
        .setPageSize(10)
        .setInitialLoadSizeHint(10)
        .build()

    private var mDataSource: DataSource<out Any, T>? = null
    private val mFactory = object : DataSource.Factory<Any, T>() {
        override fun create(): DataSource<Any, T> {
            if (mDataSource?.isInvalid != false) {
                mDataSource = createDataSource()
            }
            return mDataSource as DataSource<Any, T>
        }
    }

    private val mBoundaryPagedData = MutableLiveData<Boolean>()
    private val mCallback = object : PagedList.BoundaryCallback<T>() {
        override fun onZeroItemsLoaded() {
            mBoundaryPagedData.postValue(false)
        }

        override fun onItemAtFrontLoaded(itemAtFront: T) {
            mBoundaryPagedData.postValue(true)
        }
    }

    private val mPagedData = LivePagedListBuilder(mFactory, config)
        .setInitialLoadKey(0)
        .setBoundaryCallback(mCallback)
        .build()

    abstract fun createDataSource(): DataSource<out Any, T>

    fun getPagedData() = mPagedData

    fun getDataSource() = mDataSource

    fun getBoundaryPagedData() = mBoundaryPagedData
}