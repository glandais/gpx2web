package io.github.glandais.gpx.srtm.mapterhorn;

import io.github.glandais.gpx.srtm.mapterhorn.Projection.PixelLocation;
import java.io.IOException;

public class MapterhornElevationSource {

    private final MapterhornConfig cfg;
    private final TileLruCache cache;

    public MapterhornElevationSource(MapterhornConfig cfg) {
        this(cfg, new TileLruCache(cfg.cacheCapacity(), new HttpTileFetcher(cfg)));
    }

    public MapterhornElevationSource(MapterhornConfig cfg, TileLruCache cache) {
        this.cfg = cfg;
        this.cache = cache;
    }

    public double getEle(double latDeg, double lonDeg) throws IOException {
        int z = cfg.zoom();
        int tileSize = cfg.tileSize();
        double[] tileXY = Projection.tileXYFloat(latDeg, lonDeg, z);
        int tileX = (int) Math.floor(tileXY[0]);
        int tileY = (int) Math.floor(tileXY[1]);
        double pxFloat = (tileXY[0] - tileX) * tileSize;
        double pyFloat = (tileXY[1] - tileY) * tileSize;

        int x0 = (int) Math.floor(pxFloat);
        int y0 = (int) Math.floor(pyFloat);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        double dx = pxFloat - x0;
        double dy = pyFloat - y0;

        double e00 = sample(z, tileX, tileY, x0, y0, tileSize);
        double e10 = sample(z, tileX, tileY, x1, y0, tileSize);
        double e01 = sample(z, tileX, tileY, x0, y1, tileSize);
        double e11 = sample(z, tileX, tileY, x1, y1, tileSize);

        double top = e00 * (1.0 - dx) + e10 * dx;
        double bottom = e01 * (1.0 - dx) + e11 * dx;
        return top * (1.0 - dy) + bottom * dy;
    }

    private double sample(int z, int tileX, int tileY, int px, int py, int tileSize) throws IOException {
        PixelLocation loc = Projection.normalizePixel(z, tileX, tileY, px, py, tileSize);
        TerrainTile tile = cache.get(loc.tile());
        return tile.getElevation(loc.x(), loc.y());
    }
}
