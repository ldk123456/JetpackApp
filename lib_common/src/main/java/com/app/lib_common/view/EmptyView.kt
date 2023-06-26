package com.app.lib_common.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.LinearLayoutCompat
import com.app.lib_common.databinding.LayoutEmptyViewBinding
import com.app.lib_common.ext.setVisible

class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val binding = LayoutEmptyViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }

    fun setEmptyIcon(@DrawableRes iconRes: Int) {
        binding.ivEmptyIcon.setImageResource(iconRes)
    }

    fun setTitle(title: String?) {
        title.takeIf { !it.isNullOrEmpty() }?.let {
            binding.tvEmptyTitle.text = it
            binding.tvEmptyTitle.setVisible(true)
        } ?: run {
            binding.tvEmptyTitle.setVisible(false)
        }
    }

    fun setButton(text: String?, listener: OnClickListener? = null) {
        text.takeIf { !it.isNullOrEmpty() }?.let {
            binding.mbEmptyButton.text = it
            binding.mbEmptyButton.setOnClickListener(listener)
            binding.tvEmptyTitle.setVisible(true)
        } ?: run {
            binding.mbEmptyButton.setVisible(false)
        }
    }
}