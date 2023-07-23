package com.app.jetpack.ui.detail

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.app.jetpack.view.FullScreenPlayerView
import com.app.lib_common.R
import com.app.lib_common.ext.dp

class ViewZomBehavior : CoordinatorLayout.Behavior<FullScreenPlayerView> {
    private lateinit var mOverScroller: OverScroller
    private var mMinHeight = 200.dp
    private var mScrollingId = 0
    private var mViewDragHelper: ViewDragHelper? = null
    private var mScrollingView: View? = null
    private var mRefChild: FullScreenPlayerView? = null
    private var mChildOriginHeight = 0
    private var mViewZoomCallback: ViewZoomCallback? = null
    private var mRunnable: FlingRunnable? = null

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) {
        context.obtainStyledAttributes(attrs, R.styleable.ViewZoomBehavior, 0, 0).apply {
            mScrollingId = getResourceId(R.styleable.ViewZoomBehavior_scrolling_id, 0)
            mMinHeight = getDimensionPixelOffset(R.styleable.ViewZoomBehavior_min_height, 200.dp)
            recycle()
        }
        mOverScroller = OverScroller(context)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: FullScreenPlayerView, layoutDirection: Int): Boolean {
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback)
            mScrollingView = parent.findViewById(mScrollingId)
            mRefChild = child
            mChildOriginHeight = child.measuredHeight
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    private val mCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return (mRefChild?.bottom ?: 0) >= mMinHeight
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return mViewDragHelper?.touchSlop ?: super.getViewVerticalDragRange(child)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val refChild = mRefChild
            if (refChild == null || dy == 0) {
                return 0
            }
            if (dy < 0 && refChild.bottom < mMinHeight
                || (dy > 0 && refChild.bottom > mChildOriginHeight)
                || (dy > 0 && mScrollingView?.canScrollVertically(-1) == true)
            ) {
                return 0
            }
            var maxConsumed = dy
            if (dy > 0 && refChild.bottom + dy < mChildOriginHeight) {
                maxConsumed = mChildOriginHeight - refChild.bottom
            } else if (refChild.bottom + dy < mMinHeight) {
                maxConsumed = mMinHeight - refChild.bottom
            }
            val params = refChild.layoutParams
            params.height += maxConsumed
            refChild.layoutParams = params
            mViewZoomCallback?.onDragZoom(params.height)
            return maxConsumed
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val refChild = mRefChild ?: return
            if (refChild.bottom in (mMinHeight + 1) until mChildOriginHeight && yvel != 0f) {
                mRunnable?.let { refChild.removeCallbacks(it) }
                mRunnable = FlingRunnable(refChild)
                mRunnable?.fling(xvel.toInt(), yvel.toInt())
            }
        }
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: FullScreenPlayerView, ev: MotionEvent): Boolean {
        if (mViewDragHelper == null) {
            return super.onTouchEvent(parent, child, ev)
        }
        mViewDragHelper?.processTouchEvent(ev)
        return true
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: FullScreenPlayerView,
        ev: MotionEvent
    ): Boolean {
        if (mViewDragHelper == null) {
            return super.onInterceptTouchEvent(parent, child, ev)
        }
        return mViewDragHelper?.shouldInterceptTouchEvent(ev) ?: false
    }

    fun setViewZoomCallback(callback: ViewZoomCallback) {
        mViewZoomCallback = callback
    }

    interface ViewZoomCallback {
        fun onDragZoom(height: Int)
    }

    private inner class FlingRunnable(private val flingView: View) : Runnable {

        fun fling(xVel: Int, yVel: Int) {
            mOverScroller.fling(0, flingView.bottom, xVel, yVel, 0, Int.MAX_VALUE, 0, Int.MAX_VALUE)
            run()
        }

        override fun run() {
            val params = flingView.layoutParams
            val height = params.height
            if (mOverScroller.computeScrollOffset() && height >= mMinHeight && height <= mChildOriginHeight) {
                val newHeight = mOverScroller.currY.coerceAtMost(mChildOriginHeight)
                if (newHeight != height) {
                    params.height = newHeight
                    flingView.layoutParams = params
                    mViewZoomCallback?.onDragZoom(newHeight)
                }
                ViewCompat.postOnAnimation(flingView, this)
            } else {
                flingView.removeCallbacks(this)
            }
        }
    }
}