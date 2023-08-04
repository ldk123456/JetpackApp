package com.app.jetpack.ui.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.jetpack.model.User
import com.app.lib_common.app.AppGlobals
import com.app.lib_network.ApiService
import com.app.lib_network.cache.CacheManager
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse

object UserManager {
    private const val KEY_CACHE_USER = "cache_user"

    private val mUserLiveData = MutableLiveData<User>()
    private var mUser: User? = null

    init {
        CacheManager.getCache<User>(KEY_CACHE_USER)
            ?.takeIf { it.expires_time < System.currentTimeMillis() }
            ?.let { mUser = it }
    }

    fun save(user: User) {
        mUser = user
        CacheManager.save(KEY_CACHE_USER, user)
        if (mUserLiveData.hasObservers()) {
            mUserLiveData.postValue(user)
        }
    }

    fun login(context: Context): LiveData<User> {
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return mUserLiveData
    }

    fun isLogin() = mUser?.let { it.expires_time < System.currentTimeMillis() } ?: false

    fun getUser() = if (isLogin()) mUser else null

    fun getUserId() = getUser()?.userId ?: 0

    fun update(): LiveData<out User?> {
        if (!isLogin()) {
            return login(AppGlobals.context)
        }
        val liveData = MutableLiveData<User?>()
        ApiService.get<User>("/user/query")
            .addParam("userId", getUserId())
            .execute(object : JsonCallback<User> {
                override fun onSuccess(response: ApiResponse<User>) {
                    response.body?.let {
                        save(it)
                    }
                    liveData.postValue(response.body)
                }

                override fun onError(response: ApiResponse<User>) {
                    liveData.postValue(null)
                }
            })
        return liveData
    }

    fun logout() {
        CacheManager.delete(KEY_CACHE_USER, mUser)
        mUser = null
    }
}