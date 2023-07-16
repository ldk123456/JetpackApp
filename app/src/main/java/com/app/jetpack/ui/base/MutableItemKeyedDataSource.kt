package com.app.jetpack.ui.base

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedList

abstract class MutableItemKeyedDataSource<K : Any, V : Any>(
    private val dataSource: ItemKeyedDataSource<K, V>
) : ItemKeyedDataSource<K, V>() {
    val data = ArrayList<V>()

    @SuppressLint("RestrictedApi")
    fun buildNewPagedList(config: PagedList.Config): PagedList<V> {
        return PagedList.Builder(this, config)
            .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
            .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
            .build()
    }

    override fun loadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<V>) {
        callback.onResult(data)
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<V>) {
        dataSource.loadAfter(params, callback)
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<V>) {
        callback.onResult(emptyList())
    }
}