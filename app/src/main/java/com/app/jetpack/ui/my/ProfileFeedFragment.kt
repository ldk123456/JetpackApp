package com.app.jetpack.ui.my

import android.os.Bundle
import android.view.View
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.core.KEY_TAB_TYPE
import com.app.jetpack.core.TAB_TYPE_COMMENT
import com.app.jetpack.model.Feed
import com.app.jetpack.player.PageListPlayerDetector
import com.app.jetpack.player.PageListPlayerManager
import com.app.jetpack.ui.base.BaseListFragment
import com.app.lib_common.ext.withBundle
import com.scwang.smart.refresh.layout.api.RefreshLayout

class ProfileFeedFragment : BaseListFragment<Feed, ProfileViewModel>() {

    companion object {
        fun newInstance(tabType: String) = ProfileFeedFragment().withBundle {
            putString(KEY_TAB_TYPE, tabType)
        }
    }

    private val mTabType: String by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getString(KEY_TAB_TYPE).orEmpty()
    }
    private lateinit var mPlayDetector: PageListPlayerDetector
    private var mShouldPause = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refreshLayout.setEnableRefresh(false)
        mViewModel?.setProfileType(mTabType)
        mPlayDetector = PageListPlayerDetector(viewLifecycleOwner, binding.recyclerView)
    }

    override fun getAdapter(): PagedListAdapter<Feed, out RecyclerView.ViewHolder> {
        return object : ProfileAdapter(requireActivity(), mTabType) {
            override fun onDataViewAttachedToWindow(holder: ViewHolder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.addTarget(holder.listPlayerView)
                }
            }

            override fun onDataViewDetachedToWindow(holder: ViewHolder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.removeTarget(holder.listPlayerView)
                }
            }

            override fun onStartFeedDetailActivity(feed: Feed) {
                mShouldPause = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (TAB_TYPE_COMMENT == mTabType) {
            mPlayDetector.onPause()
        } else {
            mPlayDetector.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mShouldPause) {
            mPlayDetector.onPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PageListPlayerManager.release(mTabType)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {

    }

}