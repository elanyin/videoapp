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

/**
 * 播放器核心管理类，负责全局唯一的ExoPlayer实例的创建、管理和缓存配置。
 * 设计为单例模式 (Singleton Pattern) 的核心原因：
 * 1.  **资源复用**: 在整个应用中只维护一个播放器实例。当在不同的视频间切换时，无需频繁创建和销毁播放器，只需更换播放内容即可，极大地提升了性能和流畅度。
 * 2.  **统一管理**: 集中管理播放器的状态、缓存和生命周期，避免在多个Activity/Fragment中各自为政，导致状态混乱和资源泄漏。
 */
@UnstableApi
public class PlayerManager {

    // 使用 volatile 关键字确保多线程环境下的可见性
    private static volatile PlayerManager sInstance;

    private ExoPlayer player;
    private SimpleCache simpleCache;
    private CacheDataSource.Factory cacheDataSourceFactory;

    private final Context appContext;

    /**
     * 私有构造函数，确保只能通过 getInstance() 方法获取实例。
     * @param context 上下文
     */
    private PlayerManager(Context context) {
        // 始终使用 ApplicationContext，防止因持有Activity引用而导致的内存泄漏
        appContext = context.getApplicationContext();
        initCache();
    }

    /**
     * 获取播放器管理器的唯一实例。
     * 采用双重检查锁定 (Double-Checked Locking) 模式，确保线程安全和高性能。
     *
     * @param context 上下文
     * @return PlayerManager的单例
     */
    public static PlayerManager getInstance(Context context) {
        if (sInstance == null) { // 第一次检查，不加锁，提高性能
            synchronized (PlayerManager.class) { // 加锁，保证线程安全
                if (sInstance == null) { // 第二次检查，防止重复创建实例
                    sInstance = new PlayerManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化ExoPlayer的磁盘缓存，用于边播边存，提升二次播放的速度。
     */
    private void initCache() {
        try {
            File cacheDir = new File(appContext.getCacheDir(), "exo_cache");
            long cacheSize = 100L * 1024L * 1024L; // 100MB 缓存大小

            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(cacheSize);
            // 使用一个虽然被标记为“过时”，但非常稳定的构造函数，以保证兼容性和稳定性。
            simpleCache = new SimpleCache(cacheDir, evictor);

            DefaultDataSource.Factory upstreamFactory =
                    new DefaultDataSource.Factory(appContext);

            // 创建带缓存功能的数据源工厂
            cacheDataSourceFactory = new CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(upstreamFactory)
                    .setCacheWriteDataSinkFactory(
                            new CacheDataSink.Factory()
                                    .setCache(simpleCache)
                    );
        } catch (Exception e) {
            // 如果缓存初始化失败，则不使用缓存功能，仅记录错误
            simpleCache = null;
            cacheDataSourceFactory = null;
        }
    }

    /**
     * 获取播放器实例。这是一个同步方法，用于处理播放器可能被释放后的重建逻辑。
     * <p>
     * 这是修复“播放器已释放”导致崩溃的关键：
     * 当发现 player 为 null (已被 releasePlayer() 释放)，能自动创建一个新的ExoPlayer实例，
     * 确保后续操作的安全性。
     *
     * @return 一个可用的 ExoPlayer 实例。
     */
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

    /**
     * 准备要播放的媒体资源。
     *
     * @param uri            媒体资源的URI
     * @param playWhenReady  是否在准备好后立即播放
     * @param seekPositionMs 播放的起始位置 (毫秒)
     */
    public void prepareMedia(String uri, boolean playWhenReady, long seekPositionMs) {
        ExoPlayer currentPlayer = getPlayer(); // 总是获取一个有效的播放器实例
        MediaItem mediaItem = MediaItem.fromUri(uri);
        currentPlayer.setMediaItem(mediaItem);
        if (seekPositionMs > 0) currentPlayer.seekTo(seekPositionMs);
        currentPlayer.setPlayWhenReady(playWhenReady);
        currentPlayer.prepare();
    }

    /**
     * 释放播放器资源。此方法应在每个视频播放Activity的 onDestroy() 中调用。
     * <p>
     * 关键设计：
     * 该方法只释放 ExoPlayer 内核 (player = null)，并不会销毁 PlayerManager 单例本身 (sInstance)。
     * 这是为了让 PlayerManager 在整个App生命周期中持续存在，同时允许其管理的播放器内核被回收和重建。
     * 彻底解决了之前因销毁单例导致的状态不一致崩溃问题。
     */
    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    /**
     * 彻底关闭并清理所有资源，包括缓存和单例自身。
     * <p>
     * 这个方法应该只在整个应用程序退出时（例如在 Application 的 onTerminate()）调用一次。
     * 在常规的Activity销毁时，绝不能调用此方法。
     */
    public void shutdown() {
        releasePlayer(); // 先释放播放器
        if (simpleCache != null) {
            try {
                simpleCache.release(); // 再释放缓存
            } catch (Exception ignored) {}
            simpleCache = null;
        }
        sInstance = null; // 最后销毁单例
    }
}
