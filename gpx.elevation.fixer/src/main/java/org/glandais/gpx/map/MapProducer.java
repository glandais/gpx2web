package org.glandais.gpx.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.glandais.gpx.elevation.fixer.GPXPath;
import org.glandais.gpx.srtm.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapProducer {

	protected static final String SEPARATOR = File.separator;

	protected static final String ABC = "abc";

	protected static final Logger LOGGER = LoggerFactory.getLogger(MapProducer.class);

	protected static final MagicPower2MapSpace MAPSPACE = MagicPower2MapSpace.INSTANCE_256;

	protected int zoom;
	protected String urlPattern;
	protected BufferedImage image;

	protected double minlon;
	protected double minlat;
	protected double maxlon;
	protected double maxlat;
	protected int width;
	protected int height;

	protected Graphics2D graphics;

	protected double startx;

	protected double starty;

	protected File cache;

	private GPXPath gpxPath;

	public MapProducer(File cache, String urlPattern, GPXPath gpxPath, double margin, int zoom) {
		super();
		this.gpxPath = gpxPath;
		double minlon = gpxPath.getMinlon();
		double maxlon = gpxPath.getMaxlon();
		double minlat = gpxPath.getMinlat();
		double maxlat = gpxPath.getMaxlat();
		this.cache = new File(cache, Integer.toHexString(urlPattern.hashCode()));

		this.zoom = zoom;
		this.urlPattern = urlPattern;
		double lonmiddle = (maxlon + minlon) / 2;
		double lonwidht = (maxlon - minlon) * (1.0 + margin);
		this.minlon = lonmiddle - lonwidht / 2.0;
		this.maxlon = lonmiddle + lonwidht / 2.0;

		double latmiddle = (maxlat + minlat) / 2;
		double latwidht = (maxlat - minlat) * (1.0 + margin);
		this.minlat = latmiddle - latwidht / 2.0;
		this.maxlat = latmiddle + latwidht / 2.0;

		this.width = (int) Math
				.round(Math.abs(MAPSPACE.cLonToX(this.maxlon, zoom) - MAPSPACE.cLonToX(this.minlon, zoom)));
		this.height = (int) Math
				.round(Math.abs(MAPSPACE.cLatToY(this.maxlat, zoom) - MAPSPACE.cLatToY(this.minlat, zoom)));

		this.startx = MAPSPACE.cLonToX(this.minlon, zoom);
		this.starty = MAPSPACE.cLatToY(this.maxlat, zoom);

		if (width > 0 && height > 0) {
			LOGGER.info("Creating a map of {}x{} pixels", width, height);
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			graphics = image.createGraphics();
		}
	}

	public void export(String imgPath) throws IOException {
		compute();
		if (width > 0 && height > 0) {
			saveImage(imgPath);
		}
	}

	public void compute() throws IOException {
		if (width > 0 && height > 0) {
			fillWithImages();
			addPoints(gpxPath);
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	protected void fillWithImages() throws IOException {
		int timin = (int) Math.floor(MAPSPACE.cLonToX(this.minlon, zoom) / 256);
		int timax = (int) Math.ceil(MAPSPACE.cLonToX(this.maxlon, zoom) / 256);

		int tjmin = (int) Math.floor(MAPSPACE.cLatToY(this.maxlat, zoom) / 256);
		int tjmax = (int) Math.ceil(MAPSPACE.cLatToY(this.minlat, zoom) / 256);

		for (int i = timin; i < timax; i++) {
			for (int j = tjmin; j < tjmax; j++) {
				BufferedImage img = getImage(i, j);
				if (img != null) {
					double x = i * 256 - startx;
					double y = j * 256 - starty;
					graphics.drawImage(img, (int) x, (int) y, null);
				}
			}
		}
	}

	protected BufferedImage getImage(int i, int j) throws IOException {
		File tile = new File(this.cache, zoom + SEPARATOR + i + SEPARATOR + j);
		if (!tile.exists()) {
			String url = urlPattern.replace("{z}", "" + zoom).replace("{x}", "" + i).replace("{y}", "" + j)
					.replace("{s}", "" + ABC.charAt(ThreadLocalRandom.current().nextInt(3)));
			tile.getParentFile().mkdirs();
			LOGGER.info("Downloading {}", url);
			try {
				FileUtils.copyURLToFile(new URL(url), tile);
			} catch (FileNotFoundException e) {
				FileUtils.touch(tile);
			}
		}
		if (tile.length() == 0) {
			return null;
		} else {
			return ImageIO.read(tile);
		}
	}

	protected void addPoints(GPXPath gpxPath) {
		graphics.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		graphics.setComposite(ac);
		graphics.setColor(Color.MAGENTA);

		drawPath(gpxPath);
		drawArrows(gpxPath);
	}

	private void drawPath(GPXPath gpxPath) {
		List<Point> points = gpxPath.getPoints();

		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];
		int c = 0;
		for (Point point : points) {
			int i = (int) (MAPSPACE.cLonToX(point.getLon(), zoom) - startx);
			int j = (int) (MAPSPACE.cLatToY(point.getLat(), zoom) - starty);
			xPoints[c] = i;
			yPoints[c] = j;
			c++;
		}
		graphics.drawPolyline(xPoints, yPoints, points.size());
	}

	private void drawArrows(GPXPath gpxPath) {
		double[] dists = gpxPath.getDists();
		double length = dists[dists.length - 1];
		int count = 5;
		double arrowSizeX = 32;
		double arrowSizeY = 32;
		List<Point> points = gpxPath.getPoints();
		int c = 0;
		List<Point> checkpoints = new ArrayList<>();

		double[] targetDists = new double[count * 3];
		for (int i = 0; i < count; i++) {
			double d = (i * 2 + 1) * (length / (2.0 * count));
			targetDists[i * 3 + 1] = d;
			targetDists[i * 3] = d - 0.5;
			targetDists[i * 3 + 2] = d + 0.5;
		}

		for (int i = 0; i < dists.length - 1; i++) {
			if (dists[i] >= targetDists[c]) {
				Point p = points.get(i);
				addProjectedPoint(checkpoints, p);
				c++;
				if (c == count * 3) {
					break;
				}
			}
		}

		if (c == count * 3) {
			int[] x = new int[3];
			int[] y = new int[3];
			for (int i = 0; i < count; i++) {
				Point pm1 = checkpoints.get(i * 3);
				Point p = checkpoints.get(i * 3 + 1);
				Point pp1 = checkpoints.get(i * 3 + 2);

				double x0 = p.getLon();
				double y0 = p.getLat();

				double xdx = pp1.getLon() - pm1.getLon();
				double xdy = pp1.getLat() - pm1.getLat();
				double l = Math.sqrt(xdx * xdx + xdy * xdy);
				xdx = xdx / l;
				xdy = xdy / l;

				double ydx = -xdy;
				double ydy = xdx;

				x[0] = (int) (x0 - arrowSizeX * xdx / 2.0 + arrowSizeY * ydx);
				y[0] = (int) (y0 - arrowSizeX * xdy / 2.0 + arrowSizeY * ydy);

				x[1] = (int) (x0 + arrowSizeX * xdx / 2.0);
				y[1] = (int) (y0 + arrowSizeX * xdy / 2.0);

				x[2] = (int) (x0 - arrowSizeX * xdx / 2.0 - arrowSizeY * ydx);
				y[2] = (int) (y0 - arrowSizeX * xdy / 2.0 - arrowSizeY * ydy);

				graphics.drawPolyline(x, y, 3);
			}
		}
	}

	private void addProjectedPoint(List<Point> checkpoints, Point p) {
		double x = (int) (MAPSPACE.cLonToX(p.getLon(), zoom) - startx);
		double y = (int) (MAPSPACE.cLatToY(p.getLat(), zoom) - starty);
		checkpoints.add(new Point(x, y));
	}

	protected void saveImage(String imgPath) throws IOException {
		ImageIO.write(image, "png", new File(imgPath));
	}

}
