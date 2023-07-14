package com.app.jetpack.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.app.jetpack.R
import com.app.jetpack.core.KEY_CATEGORY
import com.app.jetpack.core.KEY_FEED
import com.app.jetpack.model.Feed
import com.app.lib_common.base.BaseActivity
import com.app.lib_common.ext.safeAs

class FeedDetailActivity : BaseActivity() {
    companion object {
        fun startActivity(context: Context, feed: Feed?, category: String) {
            val intent = Intent(context, FeedDetailActivity::class.java)
            intent.putExtra(KEY_FEED, feed)
            intent.putExtra(KEY_CATEGORY, category)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.FeedDetail_Theme)
        super.onCreate(savedInstanceState)

        val feed = intent?.getSerializableExtra(KEY_FEED).safeAs<Feed>()
        if (feed == null) {
            finish()
            return
        }

        val viewHandler = if (feed.itemType == Feed.TYPE_IMAGE) {
            ImageViewHandler(this)
        } else {
            VideoViewHandler(this)
        }
        viewHandler.bindInitData(feed)
    }
}