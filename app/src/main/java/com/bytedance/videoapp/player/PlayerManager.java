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

    private static volatile PlayerManager sInstance;

    private ExoPlayer player;
    private SimpleCache simpleCache;
    private CacheDataSource.Factory cacheDataSourceFactory;

    private final Context appContext;

    private PlayerManager(Context context) {
        appContext = context.getApplicationContext();
        initCache();
    }

    public static PlayerManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PlayerManager.class) {
                if (sInstance == null) {
                    sInstance = new PlayerManager(context);
                }
            }
        }
        return sInstance;
    }

    private void initCache() {
        try {
            File cacheDir = new File(appContext.getCacheDir(), "exo_cache");
            long cacheSize = 100L * 1024L * 1024L; // 100MB

            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(cacheSize);
            // Reverting to the older, deprecated but more stable constructor.
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

    public synchronized ExoPlayer getPlayer() {
        if (player == null) {
            DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(
                cacheDataSourceFactory != null ? cacheDataSourceFactory : new DefaultDataSource.Factory(appContext)
            );
            player = new ExoPlayer.Builder(appContext)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();
        }
        return player;
    }

    public void prepareMedia(String uri, boolean playWhenReady, long seekPositionMs) {
        ExoPlayer currentPlayer = getPlayer();
        MediaItem mediaItem = MediaItem.fromUri(uri);
        currentPlayer.setMediaItem(mediaItem);
        if (seekPositionMs > 0) currentPlayer.seekTo(seekPositionMs);
        currentPlayer.setPlayWhenReady(playWhenReady);
        currentPlayer.prepare();
    }

    // Called from Activity's onDestroy
    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // Can be called from Application's onTerminate to fully clean up.
    public void shutdown() {
        releasePlayer();
        if (simpleCache != null) {
            try {
                simpleCache.release();
            } catch (Exception ignored) {}
            simpleCache = null;
        }
        sInstance = null;
    }
}
