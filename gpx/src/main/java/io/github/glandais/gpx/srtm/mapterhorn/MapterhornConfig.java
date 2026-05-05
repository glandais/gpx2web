package io.github.glandais.gpx.srtm.mapterhorn;

import java.io.File;

public record MapterhornConfig(
        String tileUrlTemplate, int zoom, int tileSize, int cacheCapacity, File cacheRoot, String userAgent) {

    public static final String DEFAULT_TILE_URL_TEMPLATE = "https://tiles.mapterhorn.com/{z}/{x}/{y}.webp";
    public static final int DEFAULT_ZOOM = 12;
    public static final int DEFAULT_TILE_SIZE = 512;
    public static final int DEFAULT_CACHE_CAPACITY = 64;
    public static final String DEFAULT_USER_AGENT = "gpx2web (https://github.com/glandais/gpx2web)";

    public static MapterhornConfig defaults(File cacheRoot) {
        return new MapterhornConfig(
                DEFAULT_TILE_URL_TEMPLATE,
                DEFAULT_ZOOM,
                DEFAULT_TILE_SIZE,
                DEFAULT_CACHE_CAPACITY,
                cacheRoot,
                DEFAULT_USER_AGENT);
    }
}
