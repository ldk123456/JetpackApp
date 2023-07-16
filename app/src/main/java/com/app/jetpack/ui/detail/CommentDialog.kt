package com.app.jetpack.ui.detail

import android.content.Context
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.jetpack.R
import com.app.jetpack.core.KEY_ITEM_ID
import com.app.jetpack.databinding.LayoutCommentDialogBinding
import com.app.jetpack.model.Comment
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.safeAs
import com.app.lib_common.ext.withBundle
import com.app.lib_common.util.ViewHelper
import com.app.lib_common.util.ViewHelper.setViewOutline
import com.app.lib_network.ApiService
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = LayoutCommentDialogBinding.inflate(inflater, container, false)
        mBinding.commentVideo.setOnClickListener(this)
        mBinding.commentDelete.setOnClickListener(this)
        mBinding.commentSend.setOnClickListener(this)

        mBinding.root.setViewOutline(10.dp, ViewHelper.RADIUS_TOP)

        return mBinding.root
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

            }

            R.id.comment_delete -> {

            }
        }
    }

    private fun publishComment() {
        val content = mBinding.inputView.text.toString()
        if (content.isEmpty()) {
            return
        }
        ApiService.post<Comment>("/comment/addComment")
            .addParam("userId", UserManager.getUserId())
            .addParam("itemId", mItemId)
            .addParam("commentText", content)
            .addParam("image_url", "")
            .addParam("videoUrl", "")
            .addParam("width", 0)
            .addParam("height", 0)
            .execute(object : JsonCallback<Comment> {
                override fun onSuccess(response: ApiResponse<Comment>) {
                    onCommentSuccess(response.body)
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

    fun setOnCommentAddListener(listener: OnCommentAddListener) {
        mListener = listener
    }

    interface OnCommentAddListener {
        fun onCommentAdd(comment: Comment)
    }
}