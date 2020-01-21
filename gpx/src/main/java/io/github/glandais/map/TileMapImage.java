package io.github.glandais.map;

import java.io.File;

import io.github.glandais.gpx.GPXPath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TileMapImage extends MapImage {

	protected File cache;

	protected String urlPattern;

	public TileMapImage(GPXPath path, double margin, int maxSize, File cacheFolder, String urlPattern) {
		super(path, margin, maxSize);
		this.cache = new File(cacheFolder, Integer.toHexString(urlPattern.hashCode()));
		this.urlPattern = urlPattern;
	}

	public TileMapImage(GPXPath path, double margin, File cacheFolder, String urlPattern, int zoom) {
		super(path, margin);
		setZoom(zoom);
		this.cache = new File(cacheFolder, Integer.toHexString(urlPattern.hashCode()));
		this.urlPattern = urlPattern;
		initImage();
	}

}
