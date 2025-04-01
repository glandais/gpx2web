package io.github.glandais.gpx.map;

import io.github.glandais.gpx.data.GPX;
import java.io.File;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TileMapImage extends MapImage {

    protected File cache;

    protected String urlPattern;

    public TileMapImage(GPX gpx, double margin, int maxSize, File cacheFolder, String urlPattern) {
        super(gpx, margin, maxSize);
        init(cacheFolder, urlPattern);
    }

    public TileMapImage(GPX gpx, double margin, File cacheFolder, String urlPattern, int zoom) {
        super(gpx, margin);
        setZoom(zoom);
        init(cacheFolder, urlPattern);
    }

    public TileMapImage(GPX gpx, double margin, File cacheFolder, String urlPattern, Integer width, Integer height) {
        super(gpx, margin, width, height);
        init(cacheFolder, urlPattern);
    }

    private void init(File cacheFolder, String urlPattern) {
        this.cache = new File(cacheFolder, Integer.toHexString(urlPattern.hashCode()));
        this.urlPattern = urlPattern;
        initImage();
    }
}
