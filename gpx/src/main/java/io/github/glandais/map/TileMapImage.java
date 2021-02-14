package io.github.glandais.map;

import io.github.glandais.gpx.GPXPath;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class TileMapImage extends MapImage {

    protected File cache;

    protected String urlPattern;

    public TileMapImage(GPXPath path, double margin, int maxSize, File cacheFolder, String urlPattern) {
        super(path, margin, maxSize);
        init(cacheFolder, urlPattern);
    }

    public TileMapImage(GPXPath path, double margin, File cacheFolder, String urlPattern, int zoom) {
        super(path, margin);
        setZoom(zoom);
        init(cacheFolder, urlPattern);
    }

    public TileMapImage(GPXPath path, double margin, File cacheFolder, String urlPattern, Integer width,
                        Integer height) {
        super(path, margin, width, height);
        init(cacheFolder, urlPattern);
    }

    private void init(File cacheFolder, String urlPattern) {
        this.cache = new File(cacheFolder, Integer.toHexString(urlPattern.hashCode()));
        this.urlPattern = urlPattern;
        initImage();
    }

}
