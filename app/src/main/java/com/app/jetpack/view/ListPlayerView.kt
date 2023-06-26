package com.app.jetpack.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.app.jetpack.databinding.LayoutPlayerViewBinding
import com.app.lib_common.ext.getScreenHeight
import com.app.lib_common.ext.getScreenWidth
import com.app.lib_common.ext.setVisible

class ListPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = LayoutPlayerViewBinding.inflate(LayoutInflater.from(context), this)

    private var mCategory: String = ""
    private var mVideoUrl: String = ""

    fun bindData(category: String?, coverUrl: String?, videoUrl: String?, width: Int, height: Int) {
        if (coverUrl.isNullOrEmpty() || videoUrl.isNullOrEmpty()) {
            return
        }
        mCategory = category.orEmpty()
        mVideoUrl = videoUrl

        binding.ivCover.setImageUrl(coverUrl)
        if (width < height) {
            binding.ivBlurBg.setBlurImageUrl(coverUrl, 10)
            binding.ivBlurBg.setVisible(true)
        } else {
            binding.ivBlurBg.setVisible(false)
        }
        setSize(width, height)
    }

    private fun setSize(
        width: Int, height: Int,
        maxWidth: Int = getScreenWidth(),
        maxHeight: Int = getScreenHeight()
    ) {
        val layoutWidth = maxWidth
        val layoutHeight: Int
        val coverWidth: Int
        val coverHeight: Int
        if (width >= height) {
            coverWidth = maxWidth
            coverHeight = ((height / (width * 1.0f / maxWidth)).toInt())
            layoutHeight = coverHeight
        } else {
            coverHeight = maxHeight
            layoutHeight = maxHeight
            coverWidth = ((width / (height * 1.0f / maxHeight)).toInt())
        }

        layoutParams.apply {
            this.width = layoutWidth
            this.height = layoutHeight
        }
        binding.ivBlurBg.layoutParams.apply {
            this.width = layoutWidth
            this.height = layoutHeight
        }
        binding.ivCover.layoutParams.apply {
            this.width = coverWidth
            this.height = coverHeight
        }
    }
}