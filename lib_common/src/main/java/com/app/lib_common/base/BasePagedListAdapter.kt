package com.app.lib_common.base

import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.size
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

@SuppressLint("NotifyDataSetChanged")
abstract class BasePagedListAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, VH>(diffCallback) {

    companion object {
        private const val BASE_ITEM_TYPE_HEADER = 100000
        private const val BASE_ITEM_TYPE_FOOTER = 200000
    }

    private var mHeaderPosition = BASE_ITEM_TYPE_HEADER
    private var mFooterPosition = BASE_ITEM_TYPE_FOOTER

    private val mHeaders = SparseArray<View>()
    private val mFooters = SparseArray<View>()

    fun addHeaderView(view: View?) {
        view ?: return
        if (mHeaders.indexOfValue(view) < 0) {
            mHeaders[mHeaderPosition++] = view
            notifyDataSetChanged()
        }
    }

    fun addFooterView(view: View?) {
        view ?: return
        if (mFooters.indexOfValue(view) < 0) {
            mFooters[mFooterPosition++] = view
            notifyDataSetChanged()
        }
    }

    fun removeHeaderView(view: View?) {
        view ?: return
        val index = mHeaders.indexOfValue(view);
        if (index < 0) return
        mHeaders.removeAt(index)
        notifyDataSetChanged()
    }

    fun removeFooterView(view: View?) {
        view ?: return
        val index = mFooters.indexOfValue(view);
        if (index < 0) return
        mFooters.removeAt(index)
        notifyDataSetChanged()
    }

    fun getHeaderCount() = mHeaders.size

    fun getFooterCount() = mFooters.size

    override fun getItemCount(): Int {
        val itemCount = super.getItemCount()
        return itemCount + mHeaders.size + mFooters.size
    }

    fun getOriginalItemCount(): Int {
        return itemCount - mHeaders.size - mFooters.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isHeaderPosition(position)) {
            mHeaders.keyAt(position)
        } else if (isFooterPosition(position)) {
            mFooters.keyAt(position - getOriginalItemCount() - mHeaders.size)
        } else {
            getDataItemViewType(position - mHeaders.size)
        }
    }

    private fun isHeaderPosition(position: Int): Boolean {
        return position < mHeaders.size
    }

    private fun isFooterPosition(position: Int): Boolean {
        return position >= getOriginalItemCount() + mHeaders.size
    }

    open fun getDataItemViewType(position: Int): Int {
        return 0
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return if (mHeaders.indexOfKey(viewType) >= 0) {
            object : RecyclerView.ViewHolder(mHeaders[viewType]) {} as VH
        } else if (mFooters.indexOfKey(viewType) >= 0) {
            object : RecyclerView.ViewHolder(mFooters[viewType]) {} as VH
        } else {
            onCreateDataViewHolder(parent, viewType)
        }
    }

    abstract fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            return
        }
        onBindDataViewHolder(holder, position - mHeaders.size)
    }

    abstract fun onBindDataViewHolder(holder: VH, position: Int)

    override fun onViewAttachedToWindow(holder: VH) {
        val position = holder.absoluteAdapterPosition
        if (!isHeaderPosition(position) && !isFooterPosition(position)) {
            onDataViewAttachedToWindow(holder)
        }
    }

    open fun onDataViewAttachedToWindow(holder: VH) {

    }

    override fun onViewDetachedFromWindow(holder: VH) {
        val position = holder.absoluteAdapterPosition
        if (!isHeaderPosition(position) && !isFooterPosition(position)) {
            onDataViewDetachedToWindow(holder)
        }
    }

    open fun onDataViewDetachedToWindow(holder: VH) {

    }

    override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
        super.registerAdapterDataObserver(AdapterDataObserverWrapper(observer))
    }

    private inner class AdapterDataObserverWrapper(private val observer: AdapterDataObserver) : AdapterDataObserver() {
        override fun onChanged() {
            observer.onChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            observer.onItemRangeChanged(positionStart + mHeaders.size, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            observer.onItemRangeChanged(positionStart + mHeaders.size, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            observer.onItemRangeInserted(positionStart + mHeaders.size, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            observer.onItemRangeRemoved(positionStart + mHeaders.size, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            observer.onItemRangeMoved(fromPosition + mHeaders.size, toPosition + mHeaders.size, itemCount)
        }
    }
}