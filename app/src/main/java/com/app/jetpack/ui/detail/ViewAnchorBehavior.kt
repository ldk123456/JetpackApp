package com.app.jetpack.ui.detail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.app.lib_common.R
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.safeAs

class ViewAnchorBehavior : CoordinatorLayout.Behavior<View> {

    private var mAnchorId: Int = 0
    private var mExtraUsed: Int = 0

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) {
        context.obtainStyledAttributes(attrs, R.styleable.ViewAnchorBehavior, 0, 0).apply {
            mAnchorId = getResourceId(R.styleable.ViewAnchorBehavior_anchorId, 0)
            recycle()
        }
        mExtraUsed = 48.dp
    }

    constructor(anchorId: Int) {
        mAnchorId = anchorId
        mExtraUsed = 48.dp
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return mAnchorId == dependency.id
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        val anchor = parent.findViewById<View>(mAnchorId) ?: return false
        var topMargin = 0
        child.layoutParams.safeAs<CoordinatorLayout.LayoutParams>()?.apply {
            topMargin = this.topMargin
        }
        val bottom = anchor.bottom
        parent.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            0,
            parentHeightMeasureSpec,
            topMargin + bottom + mExtraUsed
        )
        return true
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val anchor = parent.findViewById<View>(mAnchorId) ?: return false
        var topMargin = 0
        child.layoutParams.safeAs<CoordinatorLayout.LayoutParams>()?.apply {
            topMargin = this.topMargin
        }
        val bottom = anchor.bottom
        parent.onLayoutChild(child, layoutDirection)
        child.offsetTopAndBottom(bottom + topMargin)
        return true
    }
}