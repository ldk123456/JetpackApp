package com.app.jetpack.ui.find

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.audiofx.DynamicsProcessing.Mbc
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.core.KEY_FEED_TYPE
import com.app.jetpack.core.KEY_TAG_FEED_LIST
import com.app.jetpack.core.KEY_TAG_LIST
import com.app.jetpack.databinding.ActivityTagFeedListBinding
import com.app.jetpack.databinding.LayoutTagFeedListHeaderBinding
import com.app.jetpack.model.Feed
import com.app.jetpack.model.TagList
import com.app.jetpack.player.PageListPlayerDetector
import com.app.jetpack.player.PageListPlayerManager
import com.app.jetpack.ui.home.FeedAdapter
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.drawable
import com.app.lib_common.util.StatusBarUtil
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener

class TagFeedListActivity : AppCompatActivity(), OnRefreshLoadMoreListener {
    companion object {
        fun startActivity(context: Context, tagList: TagList) {
            val intent = Intent(context, TagFeedListActivity::class.java).apply {
                putExtra(KEY_TAG_LIST, tagList)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var mBinding: ActivityTagFeedListBinding

    private val mTag: TagList by lazy(LazyThreadSafetyMode.NONE) {
        intent.getSerializableExtra(KEY_TAG_LIST) as? TagList ?: TagList()
    }

    private lateinit var mPlayDetector: PageListPlayerDetector
    private var mShouldPause = true
    private val mAdapter: FeedAdapter by lazy(LazyThreadSafetyMode.NONE) {
        object : FeedAdapter(this@TagFeedListActivity, KEY_TAG_FEED_LIST) {
            override fun onDataViewAttachedToWindow(holder: ViewHolder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.addTarget(holder.listPlayerView)
                }
            }

            override fun onDataViewDetachedToWindow(holder: ViewHolder) {
                mPlayDetector.removeTarget(holder.listPlayerView)
            }

            override fun onStartFeedDetailActivity(feed: Feed) {
                mShouldPause = feed.itemType != Feed.TYPE_VIDEO
            }

            override fun onCurrentListChanged(previousList: PagedList<Feed>?, currentList: PagedList<Feed>?) {
                if (previousList != null && currentList != null && !currentList.containsAll(previousList)) {
                    mBinding.refreshLayout.recyclerView.scrollToPosition(0)
                }
            }
        }
    }
    private var mTotalScrollY = 0
    private lateinit var mViewModel: TagFeedListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.fitSystemBar(this)
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tag_feed_list)
        mBinding.owner = this
        mBinding.tag = mTag
        mBinding.actionBack.setOnClickListener {
            finish()
        }
        mBinding.refreshLayout.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TagFeedListActivity)
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(this@TagFeedListActivity, LinearLayoutManager.VERTICAL).apply {
                setDrawable(R.drawable.list_divider.drawable)
            })
            itemAnimator = null
        }
        mBinding.refreshLayout.refreshLayout.setOnRefreshLoadMoreListener(this)
        mViewModel = ViewModelProvider(this)[TagFeedListViewModel::class.java]
        mViewModel.setFeedType(mTag.title)
        mViewModel.getPagedData().observe(this) {
            if (it.isNotEmpty()) {
                mAdapter.submitList(it)
            }
            finish(it.isNotEmpty())
        }
        mPlayDetector = PageListPlayerDetector(this, mBinding.refreshLayout.recyclerView)
        addHeaderView()
    }

    private fun finish(hasData: Boolean) {
        val more = hasData || mAdapter.currentList.isNullOrEmpty().not()
        mBinding.refreshLayout.emptyView.isVisible = !more
        mBinding.refreshLayout.refreshLayout.apply {
            if (state.isOpening && state.isHeader) {
                finishRefresh()
            } else if (state.isOpening && state.isFooter) {
                finishLoadMore()
            }
        }
    }

    private fun addHeaderView() {
        val header = LayoutTagFeedListHeaderBinding.inflate(layoutInflater, mBinding.refreshLayout.root, false)
        header.owner = this
        header.tag = mTag
        mAdapter.addHeaderView(header.root)
        mBinding.refreshLayout.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mTotalScrollY += dy
                val showTopBar = mTotalScrollY > 48.dp
                mBinding.apply {
                    tagLogo.isVisible = showTopBar
                    tagTitle.isVisible = showTopBar
                    topBarFollow.isVisible = showTopBar
                    actionBack.setImageResource(if (showTopBar) R.drawable.icon_back_black else R.drawable.icon_back_white)
                    topBar.setBackgroundColor(if (showTopBar) Color.WHITE else Color.TRANSPARENT)
                }
            }
        })
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.getDataSource()?.invalidate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {

    }

    override fun onResume() {
        super.onResume()
        mPlayDetector.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mShouldPause) {
            mPlayDetector.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PageListPlayerManager.release(KEY_FEED_TYPE)
    }
}