package com.app.jetpack.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.app.jetpack.databinding.ActivityLayoutLoginBinding
import com.app.jetpack.model.User
import com.app.lib_common.ext.safeAs
import com.app.lib_network.ApiService
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import com.tencent.connect.UserInfo
import com.tencent.connect.auth.QQToken
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLayoutLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivClose.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivClose -> {
                finish()
            }

            binding.btnLogin -> {
                login()
            }
        }
    }


    private var mTencent: Tencent? = null

    private fun login() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance("101794421", applicationContext)
        }
        mTencent?.login(this, "all", mLoginListener)
    }

    private val mLoginListener = object : IUiListener {
        override fun onComplete(p0: Any?) {
            runCatching {
                val response = p0 as? JSONObject ?: return@runCatching
                val openId = response.optString("openid")
                val token = response.optString("access_token")
                val expiresIn = response.optString("expires_in")
                val expiresTime = response.optLong("expires_time")

                mTencent?.apply {
                    setOpenId(openId)
                    setAccessToken(token, expiresIn)
                    getUserInfo(qqToken, expiresTime, openId)
                }
            }
        }

        override fun onError(p0: UiError?) {
            Toast.makeText(this@LoginActivity, "登录失败: ${p0?.toString()}", Toast.LENGTH_SHORT).show()
        }

        override fun onCancel() {
            Toast.makeText(this@LoginActivity, "登录取消", Toast.LENGTH_SHORT).show()
            saveDefault()
        }
    }

    private fun getUserInfo(token: QQToken?, expire: Long, openId: String?) {
        if (token == null || openId.isNullOrEmpty()) {
            return
        }
        UserInfo(applicationContext, token).getUserInfo(object : IUiListener {
            override fun onComplete(p0: Any?) {
                runCatching {
                    p0.safeAs<JSONObject>()?.let {
                        val nickname = it.optString("nickname")
                        val avatar = it.optString("figureurl_2")
                        save(nickname, avatar, openId, expire)
                    }
                }
            }

            override fun onError(p0: UiError?) {
                Toast.makeText(this@LoginActivity, "登录失败: ${p0?.toString()}", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "登录取消", Toast.LENGTH_SHORT).show()
                saveDefault()
            }
        })
    }

    private class DefaultCallback(val activity: LoginActivity) : JsonCallback<User> {
        override fun onSuccess(response: ApiResponse<User>) {
            super.onSuccess(response)
            response.body?.let {
                UserManager.save(it)
                activity.finish()
            }
        }
    }

    private fun saveDefault() {
        ApiService.get<User>("/user/query")
            .addParam("userId", 1678268374)
            .execute(DefaultCallback(this))
    }

    private fun save(nickname: String, avatar: String, openId: String, expire: Long) {
        ApiService.get<User>("/user/insert")
            .addParam("name", nickname)
            .addParam("avatar", avatar)
            .addParam("qqOpenId", openId)
            .addParam("expires_time", expire)
            .execute(object : JsonCallback<User> {
                override fun onSuccess(response: ApiResponse<User>) {
                    response.body?.let {
                        UserManager.save(it)
                        finish()
                    } ?: run {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "登录失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(response: ApiResponse<User>) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "登录失败: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mLoginListener)
        }
    }
}