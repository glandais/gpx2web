package io.github.glandais.gpx.srtm.mapterhorn;

public final class Projection {

    private Projection() {}

    public static double[] tileXYFloat(double latDeg, double lonDeg, int z) {
        double latRad = Math.toRadians(latDeg);
        double n = 1L << z;
        double xFloat = ((lonDeg + 180.0) / 360.0) * n;
        double yFloat = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n;
        return new double[] {xFloat, yFloat};
    }

    public static double[] toPixelFloat(double latDeg, double lonDeg, int z, int tileSize) {
        double[] tileXY = tileXYFloat(latDeg, lonDeg, z);
        double xFloat = tileXY[0];
        double yFloat = tileXY[1];
        int tileX = (int) Math.floor(xFloat);
        int tileY = (int) Math.floor(yFloat);
        double pxFloat = (xFloat - tileX) * tileSize;
        double pyFloat = (yFloat - tileY) * tileSize;
        return new double[] {tileX, tileY, pxFloat, pyFloat};
    }

    /**
     * Resolve an integer pixel address `(px, py)` measured against tile `(tileX, tileY)`
     * into the actual containing tile and in-tile pixel, when `(px, py)` may fall outside
     * `[0, tileSize)` because of bilinear sampling at tile boundaries.
     *
     * <p>World edges are handled by clamping to the world extent in tile units
     * (`[0, 2^z - 1]`). Going past the antimeridian or poles reuses the edge tile / pixel.
     */
    public static PixelLocation normalizePixel(int z, int tileX, int tileY, int px, int py, int tileSize) {
        int worldTiles = 1 << z;
        int newTileX = tileX;
        int newTileY = tileY;
        int newPx = px;
        int newPy = py;

        while (newPx < 0) {
            newPx += tileSize;
            newTileX -= 1;
        }
        while (newPx >= tileSize) {
            newPx -= tileSize;
            newTileX += 1;
        }
        while (newPy < 0) {
            newPy += tileSize;
            newTileY -= 1;
        }
        while (newPy >= tileSize) {
            newPy -= tileSize;
            newTileY += 1;
        }

        if (newTileX < 0) {
            newTileX = 0;
            newPx = 0;
        } else if (newTileX >= worldTiles) {
            newTileX = worldTiles - 1;
            newPx = tileSize - 1;
        }
        if (newTileY < 0) {
            newTileY = 0;
            newPy = 0;
        } else if (newTileY >= worldTiles) {
            newTileY = worldTiles - 1;
            newPy = tileSize - 1;
        }

        return new PixelLocation(new TileCoord(z, newTileX, newTileY), newPx, newPy);
    }

    public record PixelLocation(TileCoord tile, int x, int y) {}
}
