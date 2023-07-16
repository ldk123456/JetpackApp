package com.app.lib_common.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.ConcurrentHashMap

object LiveDataBus {
    private val mLiveDataMap = ConcurrentHashMap<String, StickyLiveData<*>>()

    fun <T> with(eventName: String): StickyLiveData<T> {
        var liveData = mLiveDataMap[eventName] as? StickyLiveData<T>
        if (liveData == null) {
            liveData = StickyLiveData(eventName)
            mLiveDataMap[eventName] = liveData
        }
        return liveData
    }

    class StickyLiveData<T>(private val eventName: String) : LiveData<T>() {
        private var mStickyData: T? = null

        private var mVersion = 0

        public override fun setValue(value: T?) {
            mVersion++
            super.setValue(value)
        }

        public override fun postValue(value: T?) {
            mVersion++
            super.postValue(value)
        }

        fun setStickyData(stickyData: T?) {
            mStickyData = stickyData
            value = stickyData
        }

        fun postStickyData(stickyData: T?) {
            mStickyData = stickyData
            postValue(stickyData)
        }

        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            observeSticky(owner, observer, false)
        }

        fun observeSticky(owner: LifecycleOwner, observer: Observer<in T>, isSticky: Boolean) {
            super.observe(owner, WrapperObserver(this, observer, isSticky))
            owner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (Lifecycle.Event.ON_DESTROY == event) {
                        mLiveDataMap.remove(eventName)
                    }
                }
            })
        }

        private class WrapperObserver<T>(
            val liveData: StickyLiveData<T>,
            val observer: Observer<in T>,
            val isSticky: Boolean
        ) : Observer<T> {
            private var mLastVersion = liveData.mVersion

            override fun onChanged(value: T) {
                if (mLastVersion >= liveData.mVersion) {
                    if (isSticky) {
                        liveData.mStickyData?.let {
                            observer.onChanged(it)
                        }
                    }
                    return
                }
                mLastVersion = liveData.mVersion
                observer.onChanged(value)
            }
        }
    }
}