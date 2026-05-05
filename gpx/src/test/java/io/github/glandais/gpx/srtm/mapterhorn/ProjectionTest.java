package io.github.glandais.gpx.srtm.mapterhorn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.glandais.gpx.srtm.mapterhorn.Projection.PixelLocation;
import org.junit.jupiter.api.Test;

class ProjectionTest {

    @Test
    void tileXYAtOriginIsHalfWorld() {
        // (lat=0, lon=0) at zoom 1 sits in the middle of a 2x2 tile world: (1.0, 1.0)
        double[] xy = Projection.tileXYFloat(0.0, 0.0, 1);
        assertEquals(1.0, xy[0], 1e-9);
        assertEquals(1.0, xy[1], 1e-9);
    }

    @Test
    void tileXYAtAntimeridianFarEast() {
        // (lat=0, lon=180) at zoom 1 → x = 2.0 (right edge of world)
        double[] xy = Projection.tileXYFloat(0.0, 180.0, 1);
        assertEquals(2.0, xy[0], 1e-9);
    }

    @Test
    void tileXYAtAntimeridianFarWest() {
        // (lat=0, lon=-180) at zoom 1 → x = 0.0 (left edge of world)
        double[] xy = Projection.tileXYFloat(0.0, -180.0, 1);
        assertEquals(0.0, xy[0], 1e-9);
    }

    @Test
    void ventouxLandsInExpectedTileAtZoom11() {
        // Mont Ventoux: lat 44.1739, lon 5.2783 — known tile at z=11 is (1054, 743)
        double[] xy = Projection.tileXYFloat(44.1739, 5.2783, 11);
        assertEquals(1054, (int) Math.floor(xy[0]));
        assertEquals(743, (int) Math.floor(xy[1]));
    }

    @Test
    void toPixelFloatReturnsConsistentValues() {
        int z = 11;
        int tileSize = 512;
        double[] result = Projection.toPixelFloat(44.1739, 5.2783, z, tileSize);
        int tileX = (int) result[0];
        int tileY = (int) result[1];
        double px = result[2];
        double py = result[3];

        assertEquals(1054, tileX);
        assertEquals(743, tileY);
        // Pixel coordinates must lie inside the tile
        assertEquals(true, px >= 0 && px < tileSize, "px in [0, tileSize): " + px);
        assertEquals(true, py >= 0 && py < tileSize, "py in [0, tileSize): " + py);
    }

    @Test
    void normalizePixelInsideTileIsIdentity() {
        PixelLocation loc = Projection.normalizePixel(11, 1054, 743, 100, 200, 512);
        assertEquals(1054, loc.tile().x());
        assertEquals(743, loc.tile().y());
        assertEquals(100, loc.x());
        assertEquals(200, loc.y());
    }

    @Test
    void normalizePixelNegativeXMovesToPreviousTile() {
        PixelLocation loc = Projection.normalizePixel(11, 1054, 743, -1, 100, 512);
        assertEquals(1053, loc.tile().x());
        assertEquals(743, loc.tile().y());
        assertEquals(511, loc.x());
        assertEquals(100, loc.y());
    }

    @Test
    void normalizePixelOverflowingXMovesToNextTile() {
        PixelLocation loc = Projection.normalizePixel(11, 1054, 743, 512, 100, 512);
        assertEquals(1055, loc.tile().x());
        assertEquals(743, loc.tile().y());
        assertEquals(0, loc.x());
        assertEquals(100, loc.y());
    }

    @Test
    void normalizePixelClampsAtNorthPoleEdge() {
        // At y = -1 from tile (tx, 0), we'd go to tile (tx, -1) which doesn't exist.
        // Clamp to (tx, 0) at pixel (px, 0).
        PixelLocation loc = Projection.normalizePixel(11, 100, 0, 50, -1, 512);
        assertEquals(100, loc.tile().x());
        assertEquals(0, loc.tile().y());
        assertEquals(0, loc.y());
    }

    @Test
    void normalizePixelClampsAtSouthPoleEdge() {
        int z = 1;
        int worldEdge = (1 << z) - 1; // tile index 1 is the last
        PixelLocation loc = Projection.normalizePixel(z, 0, worldEdge, 50, 600, 512);
        assertEquals(worldEdge, loc.tile().y());
        assertEquals(511, loc.y());
    }
}
