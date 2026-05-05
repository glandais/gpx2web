package io.github.glandais.gpx.srtm.mapterhorn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapterhornElevationSourceTest {

    private static final int TILE_SIZE = 512;
    private static final int ZOOM = 11;

    private static MapterhornConfig cfg() {
        return new MapterhornConfig("http://test.invalid/{z}/{x}/{y}.webp", ZOOM, TILE_SIZE, 16, null, "test");
    }

    /**
     * Encode an elevation as a Terrarium RGB int (alpha 0xFF).
     */
    private static int rgbForElevation(double meters) {
        long encoded = Math.round(meters + 32768.0); // [-32768, 32768)
        int r = (int) ((encoded >> 8) & 0xFF);
        int g = (int) (encoded & 0xFF);
        int b = 0;
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private static BufferedImage uniformTile(double meters) {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        int rgb = rgbForElevation(meters);
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }

    private static BufferedImage horizontalGradientTile(double leftMeters, double rightMeters) {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < TILE_SIZE; x++) {
            double t = x / (double) (TILE_SIZE - 1);
            double meters = leftMeters + (rightMeters - leftMeters) * t;
            int rgb = rgbForElevation(meters);
            for (int y = 0; y < TILE_SIZE; y++) {
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }

    private static class StubFetcher implements TileFetcher {
        final Map<String, BufferedImage> tiles = new HashMap<>();
        final Map<String, Integer> hits = new HashMap<>();

        void put(int z, int x, int y, BufferedImage img) {
            tiles.put(z + "/" + x + "/" + y, img);
        }

        @Override
        public TerrainTile fetch(TileCoord coord) throws IOException {
            String key = coord.cacheKey();
            hits.merge(key, 1, Integer::sum);
            BufferedImage img = tiles.get(key);
            if (img == null) {
                throw new IOException("No fixture for " + key);
            }
            return new TerrainTile(img);
        }
    }

    @Test
    void uniformTileReturnsConstantElevation() throws IOException {
        StubFetcher fetcher = new StubFetcher();
        // Mont Ventoux falls in tile (1054, 743) at zoom 11
        fetcher.put(ZOOM, 1054, 743, uniformTile(1909.0));
        // Neighboring tiles to cover bilinear corners that may cross tile boundaries
        fetcher.put(ZOOM, 1053, 743, uniformTile(1909.0));
        fetcher.put(ZOOM, 1055, 743, uniformTile(1909.0));
        fetcher.put(ZOOM, 1054, 744, uniformTile(1909.0));
        fetcher.put(ZOOM, 1054, 746, uniformTile(1909.0));

        MapterhornElevationSource source = new MapterhornElevationSource(cfg(), new TileLruCache(16, fetcher));

        double ele = source.getEle(44.1739, 5.2783);
        assertEquals(1909.0, ele, 1e-6);
    }

    @Test
    void bilinearInterpolatesBetweenAdjacentPixels() throws IOException {
        // Build a tile whose elevation increases linearly with the x coordinate.
        StubFetcher fetcher = new StubFetcher();
        fetcher.put(ZOOM, 1054, 743, horizontalGradientTile(1000.0, 2000.0));
        // Provide neighbors for boundary safety with the same gradient continuation
        fetcher.put(ZOOM, 1055, 743, horizontalGradientTile(2000.0, 3000.0));

        MapterhornElevationSource source = new MapterhornElevationSource(cfg(), new TileLruCache(16, fetcher));

        // Sample the same lat/lon the projection lands on to confirm we read the gradient.
        // The exact pixel location for Ventoux at zoom 11 ends up somewhere in the tile;
        // re-derive the expected elevation from the projection so the test stays self-consistent.
        double[] xy = Projection.tileXYFloat(44.1739, 5.2783, ZOOM);
        double pxFloat = (xy[0] - 1054) * TILE_SIZE;
        double expected = 1000.0 + (pxFloat / (TILE_SIZE - 1)) * 1000.0;

        double ele = source.getEle(44.1739, 5.2783);
        assertEquals(expected, ele, 1.0); // ±1 m tolerance for terrarium quantization
    }

    @Test
    void cachesAfterFirstFetch() throws IOException {
        StubFetcher fetcher = new StubFetcher();
        fetcher.put(ZOOM, 1054, 743, uniformTile(1500.0));
        fetcher.put(ZOOM, 1053, 743, uniformTile(1500.0));
        fetcher.put(ZOOM, 1055, 743, uniformTile(1500.0));
        fetcher.put(ZOOM, 1054, 744, uniformTile(1500.0));
        fetcher.put(ZOOM, 1054, 746, uniformTile(1500.0));

        MapterhornElevationSource source = new MapterhornElevationSource(cfg(), new TileLruCache(16, fetcher));

        source.getEle(44.1739, 5.2783);
        Map<String, Integer> firstCallHits = new HashMap<>(fetcher.hits);
        source.getEle(44.1739, 5.2783);

        // No new fetches the second time around — every tile already cached
        assertEquals(firstCallHits, fetcher.hits, "second call must hit only the in-memory cache");
    }

    @Test
    void surfacesFetchFailures() {
        StubFetcher fetcher = new StubFetcher(); // no fixtures registered
        MapterhornElevationSource source = new MapterhornElevationSource(cfg(), new TileLruCache(16, fetcher));

        assertThrows(IOException.class, () -> source.getEle(44.1739, 5.2783));
    }
}
