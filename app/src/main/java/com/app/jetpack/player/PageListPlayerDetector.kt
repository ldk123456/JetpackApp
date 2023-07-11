package com.app.jetpack.player

import android.graphics.Point
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class PageListPlayerDetector(
    owner: LifecycleOwner,
    private val recyclerView: RecyclerView
) {
    private val mTargets = ArrayList<IPlayTarget>()
    private var mPlayingTarget: IPlayTarget? = null

    private val mDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            autoPlay()
        }
    }

    init {
        owner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (Lifecycle.Event.ON_DESTROY == event) {
                    recyclerView.adapter?.unregisterAdapterDataObserver(mDataObserver)
                    owner.lifecycle.removeObserver(this)
                }
            }
        })
        recyclerView.adapter?.registerAdapterDataObserver(mDataObserver)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    autoPlay()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx == 0 && dy == 0) {
                    autoPlay()
                } else {
                    if (mPlayingTarget?.isPlaying() == true
                        && mPlayingTarget?.isTargetInBounds() == false
                    ) {
                        mPlayingTarget?.inActive()
                    }
                }
            }
        })
    }

    private fun autoPlay() {
        if (mTargets.isEmpty() || recyclerView.childCount <= 0) {
            return
        }
        if (mPlayingTarget != null && mPlayingTarget?.isPlaying() == true
            && mPlayingTarget?.isTargetInBounds() == true
        ) {
            return
        }
        mTargets.firstOrNull { it.isTargetInBounds() }?.let {
            mPlayingTarget?.inActive()
            mPlayingTarget = it
            it.onActive()
        }
    }

    private fun IPlayTarget.isTargetInBounds(): Boolean {
        val owner = getOwner() ?: return false
        ensureRecyclerViewLocation()
        val rvLocation = mRvLocation ?: return false
        if (!owner.isShown || !owner.isAttachedToWindow) {
            return false
        }
        val ownerLocation = IntArray(2)
        owner.getLocationOnScreen(ownerLocation)
        val center = ownerLocation[1] + owner.height / 2
        return center >= rvLocation.x && center <= rvLocation.y
    }

    private var mRvLocation: Point? = null
    private fun ensureRecyclerViewLocation() {
        if (mRvLocation == null) {
            val location = IntArray(2)
            recyclerView.getLocationOnScreen(location)
            mRvLocation = Point(location[1], location[1] + recyclerView.height)
        }
    }

    fun addTarget(target: IPlayTarget?) {
        target?.let { mTargets.add(it) }
    }

    fun removeTarget(target: IPlayTarget?) {
        target?.let { mTargets.remove(it) }
    }

    fun onPause() {
        mPlayingTarget?.inActive()
    }

    fun onResume() {
        mPlayingTarget?.onActive()
    }
}