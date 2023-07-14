package com.app.jetpack.ui.base

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList

class MutablePageKeyedDataSource<V : Any> : PageKeyedDataSource<Int, V>() {
    val data = ArrayList<V>()

    @SuppressLint("RestrictedApi")
    fun buildNewPagedList(config: PagedList.Config): PagedList<V> {
        return PagedList.Builder(this, config)
            .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
            .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
            .build()
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        callback.onResult(listOf(), null)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        callback.onResult(listOf(), null)
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        callback.onResult(data, null, null)
    }
}