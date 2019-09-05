package io.github.glandais.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.github.glandais.gpx.GPXPath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapImage {

	protected static final MagicPower2MapSpace MAPSPACE = MagicPower2MapSpace.INSTANCE_256;

	protected BufferedImage image;

	protected double minlon;
	protected double minlat;
	protected double maxlon;
	protected double maxlat;
	protected int width;
	protected int height;

	protected int zoom;

	protected double startx;

	protected double starty;

	protected Graphics2D graphics;

	protected MapImage(GPXPath gpxPath, double margin) {
		super();

		this.minlon = gpxPath.getMinlon();
		this.maxlon = gpxPath.getMaxlon();
		this.minlat = gpxPath.getMinlat();
		this.maxlat = gpxPath.getMaxlat();
		setZoom(16);
		
		int xmin = (int) (getX(minlon) + startx);
		int xmax = (int) (getX(maxlon) + startx);
		int ymin = (int) (getY(maxlat) + starty);
		int ymax = (int) (getY(minlat) + starty);

		int delta = (int) Math.max((xmax - xmin) * margin / 2.0, (ymax - ymin) * margin / 2.0);
		xmin = xmin - delta;
		xmax = xmax + delta;
		ymin = ymin - delta;
		ymax = ymax + delta;

		this.minlon = getLon((int) (xmin - startx));
		this.maxlon = getLon((int) (xmax - startx));
		this.maxlat = getLat((int) (ymin - starty));
		this.minlat = getLat((int) (ymax - starty));
	}

	public MapImage(GPXPath gpxPath, double margin, int maxSize) {
		this(gpxPath, margin);
		this.zoom = 0;
		do {
			setZoom(this.zoom + 1);
		} while (width < maxSize && height < maxSize);
		setZoom(this.zoom - 1);
		initImage();
	}

	protected void setZoom(int zoom) {
		this.zoom = zoom;

		this.width = ((int) Math
				.round(Math.abs(MAPSPACE.cLonToX(this.maxlon, zoom) - MAPSPACE.cLonToX(this.minlon, zoom))));
		this.height = ((int) Math
				.round(Math.abs(MAPSPACE.cLatToY(this.maxlat, zoom) - MAPSPACE.cLatToY(this.minlat, zoom))));

		this.startx = MAPSPACE.cLonToX(this.minlon, zoom);
		this.starty = MAPSPACE.cLatToY(this.maxlat, zoom);
	}

	protected void initImage() {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();
	}

	public void saveImage(File file) throws IOException {
		ImageIO.write(image, "png", file);
	}

	public double getLon(int i) {
		return MAPSPACE.cXToLon((int) (i + startx), zoom);
	}

	public double getLat(int j) {
		return MAPSPACE.cYToLat((int) (j + starty), zoom);
	}

	public int getX(double lon) {
		return (int) (MAPSPACE.cLonToX(lon, zoom) - startx);
	}

	public int getY(double lat) {
		return (int) (MAPSPACE.cLatToY(lat, zoom) - starty);
	}

	public double getTileI(double lon) {
		return MAPSPACE.cLonToX(lon, zoom) / 256.0;
	}

	public double getTileJ(double lat) {
		return MAPSPACE.cLatToY(lat, zoom) / 256.0;
	}

}
