package com.app.jetpack.ui.publish

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.jetpack.databinding.LayoutTagBottomSheetDialogBinding
import com.app.jetpack.model.TagList
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.ext.color
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.getScreenHeight
import com.app.lib_common.ext.safeAs
import com.app.lib_network.ApiService
import com.app.lib_network.core.JsonCallback
import com.app.lib_network.response.ApiResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var mBinding: LayoutTagBottomSheetDialogBinding

    private val mTagList = ArrayList<TagList>()
    private lateinit var mAdapter: TagAdapter
    private var mListener: OnTagItemSelectedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            mBinding = LayoutTagBottomSheetDialogBinding.inflate(LayoutInflater.from(context))
            mBinding.recyclerView.layoutManager = LinearLayoutManager(context)
            mAdapter = TagAdapter()
            mBinding.recyclerView.adapter = mAdapter

            setContentView(mBinding.root)
            val behavior = BottomSheetBehavior.from(mBinding.root.parent as ViewGroup)
            behavior.peekHeight = getScreenHeight() / 3
            behavior.isHideable = false

            mBinding.root.parent.safeAs<ViewGroup>()?.layoutParams?.height = (getScreenHeight() * 2f / 3).toInt()

            queryTagList()
        }
    }

    private fun queryTagList() {
        lifecycleScope.launch(Dispatchers.IO) {
            ApiService.get<List<TagList>>("/tag/queryTagList")
                .addParam("userId", UserManager.getUserId())
                .addParam("pageCount", 100)
                .addParam("tagId", 0)
                .execute(object : JsonCallback<List<TagList>> {
                    override fun onSuccess(response: ApiResponse<List<TagList>>) {
                        super.onSuccess(response)
                        response.body?.let {
                            mTagList.addAll(it)
                            lifecycleScope.launch(Dispatchers.Main) {
                                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                    mAdapter.notifyItemRangeChanged(0, mTagList.size)
                                }
                            }
                        }
                    }
                })
        }
    }

    inner class TagAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val textView = TextView(parent.context).also {
                it.textSize = 13f
                it.typeface = Typeface.DEFAULT_BOLD
                it.setTextColor(R.color.black.color)
                it.gravity = Gravity.CENTER_VERTICAL
                it.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 45.dp)
            }
            return object : RecyclerView.ViewHolder(textView) {}
        }

        override fun getItemCount(): Int {
            return mTagList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.safeAs<TextView>()?.let {
                val tag = mTagList.getOrNull(position)
                it.text = tag?.title.orEmpty()
                it.setOnClickListener {
                    tag?.apply { mListener?.onTagSelected(this) }
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    fun setOnTagItemSelectedListener(listener: OnTagItemSelectedListener) {
        mListener = listener
    }

    interface OnTagItemSelectedListener {
        fun onTagSelected(tagList: TagList)
    }
}