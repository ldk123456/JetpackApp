package com.app.jetpack.ui.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.jetpack.R
import com.app.jetpack.core.KEY_ITEM_ID
import com.app.jetpack.core.RESULT_FILE_HEIGHT
import com.app.jetpack.core.RESULT_FILE_PATH
import com.app.jetpack.core.RESULT_FILE_TYPE
import com.app.jetpack.core.RESULT_FILE_WIDTH
import com.app.jetpack.databinding.LayoutCommentDialogBinding
import com.app.jetpack.model.Comment
import com.app.jetpack.ui.login.UserManager
import com.app.jetpack.ui.publish.CaptureActivity
import com.app.jetpack.ui.publish.PreviewActivity
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.safeAs
import com.app.lib_common.ext.withBundle
import com.app.lib_common.util.FileUploadManager
import com.app.lib_common.util.FileUtil
import com.app.lib_common.util.ViewHelper
import com.app.lib_common.util.ViewHelper.setViewOutline
import com.app.lib_common.view.LoadingDialog
import com.app.lib_common.view.MyEditTextView
import com.app.lib_network.ApiService
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class CommentDialog : AppCompatDialogFragment(), View.OnClickListener {
    companion object {
        const val TAG = "CommentDialog"

        fun newInstance(itemId: Long): CommentDialog {
            return CommentDialog().withBundle {
                putLong(KEY_ITEM_ID, itemId)
            }
        }
    }

    private lateinit var mBinding: LayoutCommentDialogBinding
    private var mListener: OnCommentAddListener? = null

    private val mItemId by lazy { arguments?.getLong(KEY_ITEM_ID) ?: 0L }

    private var mFilePath = ""
    private var mWidth = 0
    private var mHeight = 0
    private var mIsVideo = false
    private var mCoverUrl = ""
    private var mFileUrl = ""
    private var mLoadingDialog: LoadingDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = LayoutCommentDialogBinding.inflate(inflater, container, false)
        mBinding.commentVideo.setOnClickListener(this)
        mBinding.commentDelete.setOnClickListener(this)
        mBinding.commentSend.setOnClickListener(this)
        mBinding.commentExtLayout.setOnClickListener(this)

        mBinding.root.setViewOutline(10.dp, ViewHelper.RADIUS_TOP)
        dismissWhenPressBack()
        return mBinding.root
    }

    private fun dismissWhenPressBack() {
        mBinding.inputView.setOnBackKeyEventListener(object : MyEditTextView.OnBackKeyEvent {
            override fun onKeyEvent(): Boolean {
                mBinding.inputView.postDelayed({ dismiss() }, 200)
                return true
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.apply {
            setWindowAnimations(0)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }
        showSoftInputMethod()
    }

    private fun showSoftInputMethod() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                delay(300)
                mBinding.inputView.apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()
                    context.getSystemService(Context.INPUT_METHOD_SERVICE)
                        .safeAs<InputMethodManager>()
                        ?.showSoftInput(this, 0)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.comment_send -> {
                publishComment()
            }

            R.id.comment_video -> {
                CaptureActivity.startActivityForResult(requireActivity())
            }

            R.id.comment_ext_layout -> {
                PreviewActivity.startActivityForResult(
                    requireActivity(), mFilePath, mIsVideo,
                    showActionBtn = false,
                    isLocalFile = true
                )
            }

            R.id.comment_delete -> {
                mFilePath = ""
                mWidth = 0
                mHeight = 0
                mIsVideo = false
                mBinding.apply {
                    commentCover.setImageDrawable(null)
                    commentExtLayout.isVisible = false
                    commentVideo.isEnabled = true
                    commentVideo.alpha = 1f
                }
            }
        }
    }

    private fun publishComment() {
        val content = mBinding.inputView.text.toString()
        if (content.isEmpty()) {
            return
        }
        if (mIsVideo && mFilePath.isNotEmpty()) {
            FileUtil.generateVideoCover(mFilePath).observe(this) {
                uploadFile(it, mFilePath)
            }
        } else if (mFilePath.isNotEmpty()) {
            uploadFile("", mFilePath)
        } else {
            publish()
        }
    }

    private fun uploadFile(coverPath: String, filePath: String) {
        showLoadingDialog()
        val count = AtomicInteger(1)
        if (coverPath.isNotEmpty()) {
            count.set(2)
            lifecycleScope.launch(Dispatchers.IO) {
                val remain = count.decrementAndGet()
                mCoverUrl = FileUploadManager.upload(coverPath).orEmpty()
                if (remain <= 0 && mFileUrl.isNotEmpty() && mCoverUrl.isNotEmpty()) {
                    publish()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val remain = count.decrementAndGet()
            mFileUrl = FileUploadManager.upload(filePath).orEmpty()
            if (remain <= 0 && mFileUrl.isNotEmpty() && (mCoverUrl.isNotEmpty() || coverPath.isEmpty())) {
                publish()
            }
        }
    }

    private fun publish() {
        val content = mBinding.inputView.text.toString()
        if (content.isEmpty()) {
            return
        }
        ApiService.post<Comment>("/comment/addComment")
            .addParam("userId", UserManager.getUserId())
            .addParam("itemId", mItemId)
            .addParam("commentText", content)
            .addParam("image_url", if (mIsVideo) mCoverUrl else mFileUrl)
            .addParam("video_url", if (mIsVideo) mFileUrl else "")
            .addParam("width", mWidth)
            .addParam("height", mHeight)
            .execute(object : JsonCallback<Comment> {
                override fun onSuccess(response: ApiResponse<Comment>) {
                    dismissLoadingDialog()
                    onCommentSuccess(response.body)
                }

                override fun onError(response: ApiResponse<Comment>) {
                    super.onError(response)
                    dismissLoadingDialog()
                }
            })
    }

    private fun onCommentSuccess(comment: Comment?) {
        comment ?: return
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(activity, "评论发布成功", Toast.LENGTH_SHORT).show()
            mListener?.onCommentAdd(comment)
            dismiss()
        }
    }

    private fun showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = LoadingDialog(requireContext()).apply {
                setLoadingText(getString(R.string.upload_text))
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }
        }
        if (mLoadingDialog?.isShowing == false) {
            mLoadingDialog?.show()
        }
    }

    private fun dismissLoadingDialog() {
        lifecycleScope.launch(Dispatchers.Main) {
            mLoadingDialog?.dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
        dismissLoadingDialog()
        mLoadingDialog = null
        mFilePath = ""
        mFileUrl = ""
        mCoverUrl = ""
        mWidth = 0
        mHeight = 0
        mIsVideo = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            mFilePath = data.getStringExtra(RESULT_FILE_PATH).orEmpty()
            mWidth = data.getIntExtra(RESULT_FILE_WIDTH, 0)
            mHeight = data.getIntExtra(RESULT_FILE_HEIGHT, 0)
            mIsVideo = !data.getBooleanExtra(RESULT_FILE_TYPE, true)

            if (mFilePath.isNotEmpty()) {
                mBinding.commentExtLayout.isVisible = true
                mBinding.commentCover.setImageUrl(mFilePath)
                if (mIsVideo) {
                    mBinding.commentIconVideo.isVisible = true
                }
            }
            mBinding.commentVideo.isEnabled = false
            mBinding.commentVideo.alpha = 0.3f
        }
    }

    fun setOnCommentAddListener(listener: OnCommentAddListener) {
        mListener = listener
    }

    interface OnCommentAddListener {
        fun onCommentAdd(comment: Comment)
    }
}