package com.app.jetpack.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class BindingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    companion object {
        @SuppressLint("CheckResult")
        @BindingAdapter(value = ["image_url", "is_circle"], requireAll = false)
        fun setImageUrl(view: BindingImageView, imageUrl: String?, isCircle: Boolean) {
            if (imageUrl.isNullOrEmpty()) {
                return
            }
            Glide.with(view).load(imageUrl).apply {
                if (isCircle) {
                    transform(CircleCrop())
                }
            }.apply {
                view.layoutParams.takeIf { it.width > 0 && it.height > 0 }?.let {
                    override(it.width, it.height)
                }
            }.into(view)
        }
    }
}