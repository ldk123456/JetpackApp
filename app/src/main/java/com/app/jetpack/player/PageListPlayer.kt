package com.app.jetpack.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.app.jetpack.R
import com.app.lib_common.app.AppGlobals
import com.app.lib_common.ext.safeAs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView

class PageListPlayer {
    var exoPlayer: ExoPlayer? = ExoPlayer.Builder(AppGlobals.context).build()
        private set
    @SuppressLint("InflateParams")
    var playerView: StyledPlayerView? = LayoutInflater.from(AppGlobals.context)
        .inflate(R.layout.layout_exo_player_view, null, false).safeAs<StyledPlayerView>()
        private set
    @SuppressLint("InflateParams")
    var playerControlView: PlayerControlView? = LayoutInflater.from(AppGlobals.context)
        .inflate(R.layout.layout_exo_player_contorller_view, null, false).safeAs<PlayerControlView>()
        private set

    var playUrl: String? = null

    init {
        playerView?.player = exoPlayer
        playerControlView?.player = exoPlayer
    }

    fun release() {
        exoPlayer?.let {
            it.playWhenReady = false
            it.stop()
            it.clearMediaItems()
            it.release()
        }
        exoPlayer = null
        playerView?.player = null
        playerView = null
        playerControlView?.player = null
        playerControlView = null
    }
}