package com.app.jetpack.ui.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.jetpack.model.User
import com.app.lib_network.cache.CacheManager

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
        context.startActivity(Intent(context, LoginActivity::class.java))
        return mUserLiveData
    }

    fun isLogin() = mUser?.let { it.expires_time < System.currentTimeMillis() } ?: false

    fun getUser() = if (isLogin()) mUser else null

    fun getUserId() = getUser()?.userId ?: 0
}