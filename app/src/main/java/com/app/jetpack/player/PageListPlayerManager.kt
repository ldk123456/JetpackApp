package com.app.jetpack.player

import com.app.lib_common.app.AppGlobals
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util

object PageListPlayerManager {
    private const val MAX_CACHE_BYTES = 100 * 1024 * 1024L

    private val sPageListPlayerMap = HashMap<String, PageListPlayer>()
    private val sMediaSourceFactory: ProgressiveMediaSource.Factory

    init {
        val application = AppGlobals.context
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(application, application.packageName))
        val cache = SimpleCache(
            application.cacheDir,
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES)
        )
        val cacheDataSinkFactory = CacheDataSink.Factory().setCache(cache)
        val cacheDateSourceFactory = CacheDataSource.Factory().setCache(cache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheWriteDataSinkFactory(cacheDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
        sMediaSourceFactory = ProgressiveMediaSource.Factory(cacheDateSourceFactory)
    }

    fun createMediaSource(url: String?): MediaSource {
        return sMediaSourceFactory.createMediaSource(MediaItem.fromUri(url.orEmpty()))
    }

    fun get(pageName: String?): PageListPlayer {
        val pageKey = pageName.orEmpty()
        val pageListPlayer = sPageListPlayerMap[pageKey]
        if (pageListPlayer != null) {
            return pageListPlayer
        }
        val player = PageListPlayer()
        sPageListPlayerMap[pageKey] = player
        return player
    }

    fun release(pageName: String?) {
        val pageKey = pageName.orEmpty()
        sPageListPlayerMap.remove(pageKey)?.release()
    }
}