package com.app.lib_common.util

import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.app.lib_common.R

object ViewHelper {
    const val RADIUS_ALL = 0
    const val RADIUS_LEFT = 1
    const val RADIUS_TOP = 2
    const val RADIUS_RIGHT = 3
    const val RADIUS_BOTTOM = 4

    fun View.setViewOutline(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        if (attrs == null || defStyleAttr == 0 || defStyleRes == 0) {
            return
        }
        val array = context.obtainStyledAttributes(attrs, R.styleable.ViewOutlineStrategy, defStyleAttr, defStyleRes)
        val radius = array.getDimensionPixelSize(R.styleable.ViewOutlineStrategy_radius, 0)
        val radiusSide = array.getInt(R.styleable.ViewOutlineStrategy_radiusSide, -1)
        array.recycle()
        setViewOutline(radius, radiusSide)
    }

    fun View.setViewOutline(radius: Int, radiusSide: Int) {
        if (radius == 0 || radiusSide == -1) {
            return
        }
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                val w = width
                val h = height
                if (w == 0 || h == 0) {
                    return
                }
                var left = 0
                var top = 0
                var right = w
                var bottom = h
                when (radiusSide) {
                    RADIUS_LEFT -> {
                        right += radius
                    }

                    RADIUS_TOP -> {
                        bottom += radius
                    }

                    RADIUS_RIGHT -> {
                        left -= radius
                    }

                    RADIUS_BOTTOM -> {
                        top -= radius
                    }
                }
                outline?.setRoundRect(left, top, right, bottom, radius.toFloat())
            }
        }
        clipToOutline = true
        invalidate()
    }
}