package com.app.jetpack.ui.publish

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.alibaba.fastjson2.JSONObject
import com.app.jetpack.R
import com.app.jetpack.core.KEY_FILE_PATH
import com.app.jetpack.core.KEY_FILE_URL
import com.app.jetpack.core.PATH_MAIN_PUBLISH
import com.app.jetpack.core.RESULT_FILE_HEIGHT
import com.app.jetpack.core.RESULT_FILE_PATH
import com.app.jetpack.core.RESULT_FILE_TYPE
import com.app.jetpack.core.RESULT_FILE_WIDTH
import com.app.jetpack.databinding.ActivityPublishBinding
import com.app.jetpack.model.Feed
import com.app.jetpack.model.TagList
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.util.FileUtil
import com.app.lib_common.view.LoadingDialog
import com.app.lib_nav_annotation.annotation.ActivityDestination
import com.app.lib_network.ApiService
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@ActivityDestination(PATH_MAIN_PUBLISH)
class PublishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityPublishBinding

    private var mWidth = 0
    private var mHeight = 0
    private var mFilePath = ""
    private var mIsVideo = false

    private var mTagList: TagList? = null

    private var mCoverFilePath = ""
    private var mCoverUploadUUID: UUID? = null
    private var mFileUploadUUID: UUID? = null
    private var mCoverUploadUrl = ""
    private var mFileUploadUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        fitStatusBar()
        super.onCreate(savedInstanceState)
        mBinding = ActivityPublishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.actionClose.setOnClickListener(this)
        mBinding.actionPublish.setOnClickListener(this)
        mBinding.actionDeleteFile.setOnClickListener(this)
        mBinding.actionAddTag.setOnClickListener(this)
        mBinding.actionAddFile.setOnClickListener(this)
    }

    private fun fitStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        val insets = WindowInsetsControllerCompat(window, window.decorView)
        insets.hide(WindowInsetsCompat.Type.statusBars())
        insets.isAppearanceLightStatusBars = true
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.actionClose -> {
                showExitDialog()
            }

            mBinding.actionPublish -> {
                publish()
            }

            mBinding.actionAddTag -> {
                addTag()
            }

            mBinding.actionAddFile -> {
                CaptureActivity.startActivityForResult(this)
            }

            mBinding.actionDeleteFile -> {
                onDeleteFile()
            }
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.publish_exit_message)
            .setNegativeButton(R.string.publish_exit_action_cancel, null)
            .setPositiveButton(R.string.publish_exit_action_ok) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .create()
            .show()
    }

    private fun addTag() {
        TagBottomSheetDialogFragment().apply {
            setOnTagItemSelectedListener(object : TagBottomSheetDialogFragment.OnTagItemSelectedListener {
                override fun onTagSelected(tagList: TagList) {
                    mTagList = tagList
                    mBinding.actionAddTag.text = tagList.title
                }
            })
        }.show(supportFragmentManager, "tag_dialog")
    }

    private fun onDeleteFile() {
        mBinding.actionAddFile.isVisible = true
        mBinding.fileContainer.isVisible = false
        mBinding.cover.setImageDrawable(null)
        mFilePath = ""
        mWidth = 0
        mHeight = 0
        mIsVideo = false
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (CaptureActivity.REQ_CAPTURE == requestCode && RESULT_OK == resultCode && data != null) {
            mWidth = data.getIntExtra(RESULT_FILE_WIDTH, 0)
            mHeight = data.getIntExtra(RESULT_FILE_HEIGHT, 0)
            mFilePath = data.getStringExtra(RESULT_FILE_PATH).orEmpty()
            mIsVideo = !data.getBooleanExtra(RESULT_FILE_TYPE, true)
            showFileThumbnail()
        }
    }

    private fun showFileThumbnail() {
        if (mFilePath.isEmpty()) {
            return
        }
        mBinding.actionAddFile.isVisible = false
        mBinding.fileContainer.isVisible = true
        mBinding.cover.setImageUrl(mFilePath)
        mBinding.videoIcon.isVisible = mIsVideo
        mBinding.cover.setOnClickListener {
            PreviewActivity.startActivityForResult(this, mFilePath, mIsVideo, showActionBtn = false, isLocalFile = true)
        }
    }

    private fun publish() {
        showLoading()
        if (mFilePath.isEmpty()) {
            publishFeed()
            return
        }
        val workRequests = ArrayList<OneTimeWorkRequest>()
        if (mIsVideo) {
            FileUtil.generateVideoCover(mFilePath).observe(this) {
                mCoverFilePath = it
                val request = getOneTimeWorkRequest(it)
                mCoverUploadUUID = request.id
                workRequests.add(request)
                enqueue(workRequests)
            }
        }
        val request = getOneTimeWorkRequest(mFilePath)
        mFileUploadUUID = request.id
        workRequests.add(request)
        if (!mIsVideo) {
            enqueue(workRequests)
        }
    }

    private fun enqueue(workRequests: List<OneTimeWorkRequest>) {
        val workContinuation = WorkManager.getInstance(this).beginWith(workRequests)
        workContinuation.enqueue()
        workContinuation.workInfosLiveData.observe(this) {
            var completedCount = 0
            it.forEach { work ->
                if (WorkInfo.State.SUCCEEDED == work.state) {
                    val fileUrl = work.outputData.getString(KEY_FILE_URL).orEmpty()
                    if (work.id == mCoverUploadUUID) {
                        mCoverUploadUrl = fileUrl
                    } else if (work.id == mFileUploadUUID) {
                        mFileUploadUrl = fileUrl
                    }
                    completedCount++
                }
            }
            if (completedCount >= it.size) {
                publishFeed()
            }
        }
    }

    private fun getOneTimeWorkRequest(filePath: String): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString(KEY_FILE_PATH, filePath)
            .build()
        return OneTimeWorkRequest.Builder(UploadFileWorker::class.java)
            .setInputData(inputData)
            .build()
    }

    private fun publishFeed() {
        ApiService.post<JSONObject>("/feeds/publish")
            .addParam("coverUrl", mCoverUploadUrl)
            .addParam("fileUrl", mFileUploadUrl)
            .addParam("fileWidth", mWidth)
            .addParam("fileHeight", mHeight)
            .addParam("userId", UserManager.getUserId())
            .addParam("tagId", mTagList?.tagId ?: 0)
            .addParam("tagTitle", mTagList?.title.orEmpty())
            .addParam("feedText", mBinding.inputView.text.toString())
            .addParam("feedType", if (mIsVideo) Feed.TYPE_VIDEO else Feed.TYPE_IMAGE)
            .execute(object : JsonCallback<JSONObject> {
                override fun onSuccess(response: ApiResponse<JSONObject>) {
                    super.onSuccess(response)
                    dismissLoading()
                    finish()
                }
            })
    }

    private var mLoadingDialog: LoadingDialog? = null

    private fun showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog(this)
            mLoadingDialog?.setLoadingText(getString(R.string.feed_publish_ing))
        }
        mLoadingDialog?.show()
    }

    private fun dismissLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            mLoadingDialog?.dismiss()
        }
    }
}