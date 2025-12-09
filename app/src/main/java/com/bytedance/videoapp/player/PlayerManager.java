package com.bytedance.videoapp.player;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheDataSink;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import java.io.File;

@UnstableApi
public class PlayerManager {

    private static PlayerManager sInstance;

    private final ExoPlayer player;
    private SimpleCache simpleCache;
    private CacheDataSource.Factory cacheDataSourceFactory;

    private final Context appContext;

    private PlayerManager(Context context) {
        appContext = context.getApplicationContext();
        initCache();

        // 用缓存 DataSource 作为 MediaSourceFactory
        player = new ExoPlayer.Builder(appContext)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory))
                .build();
    }

    public static synchronized PlayerManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PlayerManager(context);
        }
        return sInstance;
    }

    /**
     * 初始化磁盘缓存（100MB）
     */
    private void initCache() {
        try {
            File cacheDir = new File(appContext.getCacheDir(), "exo_cache");
            long cacheSize = 100L * 1024L * 1024L; // 100MB

            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(cacheSize);
            simpleCache = new SimpleCache(cacheDir, evictor);

            DefaultDataSource.Factory upstreamFactory =
                    new DefaultDataSource.Factory(appContext);

            cacheDataSourceFactory = new CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(upstreamFactory)
                    .setCacheWriteDataSinkFactory(
                            new CacheDataSink.Factory()
                                    .setCache(simpleCache)
                    );
        } catch (Exception e) {
            simpleCache = null;
            cacheDataSourceFactory = null;
        }
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * 准备播放
     */
    public void prepareMedia(String uri, boolean playWhenReady, long seekPositionMs) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        if (seekPositionMs > 0) player.seekTo(seekPositionMs);
        player.setPlayWhenReady(playWhenReady);
        player.prepare();
    }

    public void release() {
        player.release();
        if (simpleCache != null) {
            try {
                simpleCache.release();
            } catch (Exception ignored) {}
            simpleCache = null;
        }
        sInstance = null;
    }
}
