package io.github.glandais.gpx.srtm.mapterhorn;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class TileLruCache {

    private final int capacity;
    private final TileFetcher fetcher;
    private final LinkedHashMap<String, TerrainTile> map;
    private final ConcurrentHashMap<String, CompletableFuture<TerrainTile>> inflight = new ConcurrentHashMap<>();

    public TileLruCache(int capacity, TileFetcher fetcher) {
        this.capacity = capacity;
        this.fetcher = fetcher;
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, TerrainTile> eldest) {
                return size() > TileLruCache.this.capacity;
            }
        };
    }

    public TerrainTile get(TileCoord coord) throws IOException {
        String key = coord.cacheKey();
        synchronized (map) {
            TerrainTile cached = map.get(key);
            if (cached != null) {
                return cached;
            }
        }

        CompletableFuture<TerrainTile> future = inflight.computeIfAbsent(
                key,
                k -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return fetcher.fetch(coord);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
        future.whenComplete((tile, err) -> inflight.remove(key));

        TerrainTile tile;
        try {
            tile = future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof UncheckedIOException uio) {
                throw uio.getCause();
            }
            if (cause instanceof IOException io) {
                throw io;
            }
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new IOException("Failed to load tile " + key, cause);
        }

        synchronized (map) {
            map.put(key, tile);
        }
        return tile;
    }
}
