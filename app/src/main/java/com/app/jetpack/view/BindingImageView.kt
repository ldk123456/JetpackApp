package com.app.jetpack.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.getScreenWidth
import com.app.lib_common.ext.safeAs
import com.app.lib_common.ext.setVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation

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

class BindingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    fun setImageUrl(imageUrl: String?) {
        setImageUrl(this, imageUrl, false)
    }

    fun setBlurImageUrl(coverUrl: String?, radius: Int) {
        if (coverUrl.isNullOrEmpty()) {
            return
        }
        Glide.with(this).load(coverUrl)
            .override(50)
            .transform(BlurTransformation())
            .dontAnimate()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    placeholder?.let {
                        background = it
                    }
                }
            })
    }

    fun bindData(
        imageUrl: String?,
        width: Int, height: Int,
        marginStart: Int,
        maxWidth: Int = getScreenWidth(),
        maxHeight: Int = getScreenWidth()
    ) {
        if (imageUrl.isNullOrEmpty()) {
            setVisible(false)
            return
        }
        setVisible(true)
        if (width <= 0 || height <= 0) {
            Glide.with(this).load(imageUrl).into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    setSize(resource.intrinsicWidth, resource.intrinsicHeight, marginStart, maxWidth, maxHeight)
                    setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        } else {
            setSize(width, height, marginStart, maxWidth, maxHeight)
            setImageUrl(imageUrl)
        }
    }

    private fun setSize(
        width: Int, height: Int,
        marginStart: Int,
        maxWidth: Int, maxHeight: Int
    ) {
        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = maxWidth
            finalHeight = ((height / (width * 1.0f / finalWidth)).toInt())
        } else {
            finalHeight = maxHeight
            finalWidth = ((width / (height * 1.0f / finalHeight)).toInt())
        }

        val leftMargin = if (height > width) marginStart.dp else 0
        layoutParams.apply {
            this.width = finalWidth
            this.height = finalHeight
            takeIf { it is LinearLayoutCompat.LayoutParams }?.let {
                it.safeAs<LinearLayoutCompat.LayoutParams>()?.leftMargin = leftMargin
            } ?: takeIf { it is ConstraintLayout.LayoutParams }?.let {
                it.safeAs<ConstraintLayout.LayoutParams>()?.leftMargin = leftMargin
            } ?: takeIf { it is FrameLayout.LayoutParams }?.let {
                it.safeAs<FrameLayout.LayoutParams>()?.leftMargin = leftMargin
            }
        }
    }
}