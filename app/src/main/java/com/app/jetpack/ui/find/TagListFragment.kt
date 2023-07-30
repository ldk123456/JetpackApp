package com.app.jetpack.ui.find

import android.os.Bundle
import android.view.View
import androidx.paging.ItemKeyedDataSource
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.core.KEY_TAG_TYPE
import com.app.jetpack.model.TagList
import com.app.jetpack.ui.base.BaseListFragment
import com.app.jetpack.ui.base.MutableItemKeyedDataSource
import com.app.lib_common.ext.safeAs
import com.app.lib_common.ext.withBundle
import com.scwang.smart.refresh.layout.api.RefreshLayout

class TagListFragment : BaseListFragment<TagList, TagListViewModel>() {
    companion object {
        fun newInstance(tagType: String): TagListFragment {
            return TagListFragment().withBundle {
                putString(KEY_TAG_TYPE, tagType)
            }
        }
    }

    private val mTagType: String by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getString(KEY_TAG_TYPE).orEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ("onlyFollow" == mTagType) {
            binding.emptyView.apply {
                setTitle(getString(R.string.tag_list_no_follow))
                setButton(getString(R.string.tag_list_no_follow_button)) {
                    mViewModel?.switchTabLiveData?.value = true
                }
            }
        }
        binding.recyclerView.removeItemDecorationAt(0)
        mViewModel?.setTagType(mTagType)

    }

    override fun getAdapter(): PagedListAdapter<TagList, out RecyclerView.ViewHolder> {
        return TagListAdapter(requireActivity())
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel?.getDataSource()?.invalidate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        val currentList = mAdapter.currentList ?: return
        val tagId = currentList.lastOrNull()?.tagId ?: 0
        mViewModel?.loadData(tagId, object : ItemKeyedDataSource.LoadCallback<TagList>() {
            override fun onResult(data: List<TagList>) {
                if (data.isNotEmpty()) {
                    val dataSource =
                        object : MutableItemKeyedDataSource<Long, TagList>(mViewModel?.getDataSource().safeAs()!!) {
                            override fun getKey(item: TagList): Long {
                                return item.tagId
                            }
                        }
                    dataSource.data.addAll(currentList)
                    dataSource.data.addAll(data)
                    submitList(dataSource.buildNewPagedList(currentList.config))
                } else {
                    finishRefresh(false)
                }
            }
        })
    }
}