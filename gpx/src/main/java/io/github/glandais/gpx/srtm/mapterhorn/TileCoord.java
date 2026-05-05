package io.github.glandais.gpx.srtm.mapterhorn;

public record TileCoord(int z, int x, int y) {

    public String cacheKey() {
        return z + "/" + x + "/" + y;
    }
}
