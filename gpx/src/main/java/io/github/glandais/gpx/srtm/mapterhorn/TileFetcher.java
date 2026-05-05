package io.github.glandais.gpx.srtm.mapterhorn;

import java.io.IOException;

public interface TileFetcher {

    TerrainTile fetch(TileCoord coord) throws IOException;
}
