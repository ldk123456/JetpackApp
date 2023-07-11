package com.app.jetpack.player

import android.view.ViewGroup

interface IPlayTarget {
    fun getOwner(): ViewGroup?

    fun onActive()

    fun inActive()

    fun isPlaying(): Boolean
}