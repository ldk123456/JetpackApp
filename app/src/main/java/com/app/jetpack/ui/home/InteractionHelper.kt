package com.app.jetpack.ui.home

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alibaba.fastjson2.JSONObject
import com.app.jetpack.core.DATA_FROM_INTERACTION
import com.app.jetpack.model.Comment
import com.app.jetpack.model.Feed
import com.app.jetpack.model.TagList
import com.app.jetpack.model.User
import com.app.jetpack.ui.ShareDialog
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.app.AppGlobals
import com.app.lib_common.app.LiveDataBus
import com.app.lib_network.ApiService
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import java.util.Date

object InteractionHelper {
    private const val URL_TOGGLE_FEED_LIK = "/ugc/toggleFeedLike";

    private const val URL_TOGGLE_FEED_DISS = "/ugc/dissFeed"

    private const val URL_SHARE = "/ugc/increaseShareCount"

    private const val URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike"

    @JvmStatic
    fun toggleFeedLike(owner: LifecycleOwner, feed: Feed) {
        doAfterLogin(owner) {
            ApiService.get<JSONObject>(URL_TOGGLE_FEED_LIK)
                .addParam("userId", UserManager.getUserId())
                .addParam("itemId", feed.itemId)
                .execute(object : JsonCallback<JSONObject> {
                    override fun onSuccess(response: ApiResponse<JSONObject>) {
                        response.body?.let {
                            feed.ugc?.hasLiked = it.getBoolean("hasLiked") ?: false
                            LiveDataBus.with<Feed>(DATA_FROM_INTERACTION).postValue(feed)
                        }
                    }
                })
        }
    }

    @JvmStatic
    fun toggleFeedDiss(owner: LifecycleOwner, feed: Feed) {
        doAfterLogin(owner) {
            ApiService.get<JSONObject>(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.getUserId())
                .addParam("itemId", feed.itemId)
                .execute(object : JsonCallback<JSONObject> {
                    override fun onSuccess(response: ApiResponse<JSONObject>) {
                        response.body?.let {
                            feed.ugc?.hasdiss = it.getBoolean("hasLiked") ?: false
                        }
                    }
                })
        }
    }

    @JvmStatic
    fun toggleCommentLike(owner: LifecycleOwner, comment: Comment) {
        doAfterLogin(owner) {
            ApiService.get<JSONObject>(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.getUserId())
                .execute(object : JsonCallback<JSONObject> {
                    override fun onSuccess(response: ApiResponse<JSONObject>) {
                        response.body?.let {
                            comment.ugc?.hasLiked = it.getBoolean("hasLiked") ?: false
                        }
                    }
                })
        }
    }

    private fun doAfterLogin(owner: LifecycleOwner, block: () -> Unit) {
        if (!UserManager.isLogin()) {
            UserManager.login(AppGlobals.context).apply {
                observe(owner, object : Observer<User> {
                    override fun onChanged(value: User) {
                        block()
                        removeObserver(this)
                    }
                })
            }
        } else {
            block()
        }
    }

    @JvmStatic
    fun openShare(context: Context, feed: Feed) {
        val url = "https://h5.pipix.com/item/${feed.itemId}?app_id=1319&app=super" +
                "&timestamp=${Date().time}&user_id=${UserManager.getUserId()}"
        ShareDialog(context)
            .setShareContent(url)
            .setShareItemClickListener {
                ApiService.get<JSONObject>(URL_SHARE)
                    .addParam("itemId", feed.itemId)
                    .execute(object : JsonCallback<JSONObject> {
                        override fun onSuccess(response: ApiResponse<JSONObject>) {
                            response.body?.let {
                                feed.ugc?.shareCount = it.getIntValue("count")
                                LiveDataBus.with<Feed>(DATA_FROM_INTERACTION).postValue(feed)
                            }
                        }
                    })
            }.show()
    }

    @JvmStatic
    fun toggleFeedFavorite(owner: LifecycleOwner, feed: Feed) {
        doAfterLogin(owner) {
            ApiService.get<JSONObject>("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.getUserId())
                .execute(object : JsonCallback<JSONObject> {
                    override fun onSuccess(response: ApiResponse<JSONObject>) {
                        response.body?.let {
                            feed.ugc?.hasFavorite = it.getBoolean("hasFavorite") ?: false
                            LiveDataBus.with<Feed>(DATA_FROM_INTERACTION).postValue(feed)
                        }
                    }
                })
        }
    }

    @JvmStatic
    fun toggleFollowUser(owner: LifecycleOwner, feed: Feed) {
        doAfterLogin(owner) {
            ApiService.get<JSONObject>("/ugc/toggleUserFollow")
                .addParam("followUserId", UserManager.getUserId())
                .addParam("userId", feed.author?.userId ?: 0)
                .execute(object : JsonCallback<JSONObject> {
                    override fun onSuccess(response: ApiResponse<JSONObject>) {
                        response.body?.let {
                            feed.author?.hasFollow = it.getBoolean("hasLiked") ?: false
                            LiveDataBus.with<Feed>(DATA_FROM_INTERACTION).postValue(feed)
                        }
                    }
                })
        }
    }

    @JvmStatic
    fun deleteFeedComment(context: Context, itemId: Long, commentId: Long): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        AlertDialog.Builder(context)
            .setNegativeButton("删除") { dialog, _ ->
                dialog.dismiss()
                deleteFeedComment(liveData, itemId, commentId)
            }
            .setPositiveButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .setMessage("确定要删除这条评论吗？")
            .create().show()
        return liveData
    }

    private fun deleteFeedComment(liveData: MutableLiveData<Boolean>, itemId: Long, commentId: Long) {
        ApiService.get<JSONObject>("/comment/deleteComment")
            .addParam("userId", UserManager.getUserId())
            .addParam("commentId", commentId)
            .addParam("itemId", itemId)
            .execute(object : JsonCallback<JSONObject> {
                override fun onSuccess(response: ApiResponse<JSONObject>) {
                    response.body?.let {
                        val result = it.getBooleanValue("result", false)
                        liveData.postValue(result)
                    }
                }
            })
    }

    @JvmStatic
    fun toggleTagLike(owner: LifecycleOwner, tagList: TagList) {
        doAfterLogin(owner) {
            ApiService.get<JSONObject>("/tag/toggleTagFollow")
                .addParam("userId", UserManager.getUserId())
                .addParam("tagId", tagList.tagId)
                .execute(object : JsonCallback<JSONObject> {
                    override fun onSuccess(response: ApiResponse<JSONObject>) {
                        response.body?.let {
                            tagList.hasFollow = it.getBooleanValue("hasFollow")
                        }
                    }
                })
        }
    }

    @JvmStatic
    fun deleteFeed(activity: FragmentActivity, itemId: Long): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        AlertDialog.Builder(activity)
            .setMessage("确定要删除该条帖子吗？？")
            .setNegativeButton("删除") { dialog, _ ->
                dialog.dismiss();
                deleteFeedInternal(itemId, liveData);
            }.setPositiveButton("取消", null)
            .create().show();
        return liveData
    }

    private fun deleteFeedInternal(itemId: Long, liveData: MutableLiveData<Boolean>) {
        ApiService.get<JSONObject>("/feeds/deleteFeed")
            .addParam("itemId", itemId)
            .addParam("userId", UserManager.getUserId())
            .execute(object : JsonCallback<JSONObject> {
                override fun onSuccess(response: ApiResponse<JSONObject>) {
                    response.body?.let {
                        liveData.postValue(it.getBooleanValue("result"))
                    }
                }
            })
    }
}