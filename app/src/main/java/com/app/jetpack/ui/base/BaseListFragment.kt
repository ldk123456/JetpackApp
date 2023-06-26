package com.app.jetpack.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.databinding.LayoutRefreshViewBinding
import com.app.lib_common.ext.drawable
import com.app.lib_common.ext.safeAs
import com.app.lib_common.ext.setVisible
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import java.lang.reflect.ParameterizedType

abstract class BaseListFragment<T : Any, M : BasePagedViewModel<T>>
    : Fragment(), OnRefreshLoadMoreListener {

    private lateinit var binding: LayoutRefreshViewBinding

    protected lateinit var mAdapter: PagedListAdapter<T, out RecyclerView.ViewHolder>
    protected var mViewModel: M? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LayoutRefreshViewBinding.inflate(inflater)

        initRefreshView()
        initRecyclerView()
        genericViewModel()

        return binding.root
    }

    private fun initRefreshView() {
        binding.refreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setOnRefreshLoadMoreListener(this@BaseListFragment)
        }
    }

    private fun initRecyclerView() {
        mAdapter = getAdapter()
        binding.recyclerView.apply {
            adapter = mAdapter
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).also {
                it.setDrawable(R.drawable.list_divider.drawable)
            })
        }
    }

    abstract fun getAdapter(): PagedListAdapter<T, out RecyclerView.ViewHolder>

    private fun genericViewModel() {
        javaClass.genericSuperclass.safeAs<ParameterizedType>()?.actualTypeArguments
            ?.getOrNull(1)?.safeAs<Class<*>>()?.asSubclass(BasePagedViewModel::class.java)
            ?.let { clazz ->
                mViewModel = ViewModelProvider(this)[clazz].safeAs()
                mViewModel?.getPagedData()?.observe(viewLifecycleOwner) {
                    submitList(it)
                }
                mViewModel?.getBoundaryPagedData()?.observe(viewLifecycleOwner) {
                    finishRefresh(it)
                }
            }
    }

    open fun submitList(result: PagedList<T>?) {
        if (!result.isNullOrEmpty()) {
            mAdapter.submitList(result)
        }
        finishRefresh(!result.isNullOrEmpty())
    }

    open fun finishRefresh(hasData: Boolean) {
        val state = binding.refreshLayout.state
        if (state.isFooter && state.isOpening) {
            binding.refreshLayout.finishLoadMore()
        } else if (state.isHeader && state.isOpening) {
            binding.refreshLayout.finishRefresh()
        }
        binding.emptyView.setVisible(!hasData && mAdapter.currentList.isNullOrEmpty())
    }
}