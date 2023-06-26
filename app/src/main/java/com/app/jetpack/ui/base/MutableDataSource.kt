package com.app.jetpack.ui.base

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList

class MutableDataSource<K : Any, V : Any> : PageKeyedDataSource<K, V>() {
    val data = ArrayList<V>()

    @SuppressLint("RestrictedApi")
    fun buildNewPagedList(config: PagedList.Config): PagedList<V> {
        return PagedList.Builder(this, config)
            .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
            .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
            .build()
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        callback.onResult(listOf(), null)
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        callback.onResult(listOf(), null)
    }

    override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        callback.onResult(data, null, null)
    }
}