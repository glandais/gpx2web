package org.glandais.gpx.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glandais.gpx.srtm.Point;

public class MapProducer {

	private static final File CACHE = new File("C:\\gpx\\cache");

	private static final MagicPower2MapSpace MAPSPACE = MagicPower2MapSpace.INSTANCE_256;
	private int zoom;
	private String urlPattern;
	private BufferedImage image;

	private double minlon;
	private double minlat;
	private double maxlon;
	private double maxlat;
	private int width;
	private int height;

	private Graphics2D graphics;

	private double startx;

	private double starty;

	public MapProducer(String urlPattern, double minlon, double maxlon, double minlat, double maxlat, double margin,
			int zoom) {
		super();
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

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();
	}

	public void fillWithImages() throws IOException {
		int timin = (int) Math.floor(MAPSPACE.cLonToX(this.minlon, zoom) / 256);
		int timax = (int) Math.ceil(MAPSPACE.cLonToX(this.maxlon, zoom) / 256);

		int tjmin = (int) Math.floor(MAPSPACE.cLatToY(this.maxlat, zoom) / 256);
		int tjmax = (int) Math.ceil(MAPSPACE.cLatToY(this.minlat, zoom) / 256);

		for (int i = timin; i < timax; i++) {
			for (int j = tjmin; j < tjmax; j++) {
				BufferedImage img = getImage(i, j);
				double x = i * 256 - startx;
				double y = j * 256 - starty;
				graphics.drawImage(img, (int) x, (int) y, null);
			}
		}
	}

	private BufferedImage getImage(int i, int j) throws IOException {
		File image = new File(CACHE, zoom + "/" + i + "/" + j);
		if (!image.exists()) {
			String url = urlPattern.replace("{z}", "" + zoom).replace("{x}", "" + i).replace("{y}", "" + j);
			image.getParentFile().mkdirs();
			System.out.println("Downloading " + url);
			FileUtils.copyURLToFile(new URL(url), image);
		}
		return ImageIO.read(image);
	}

	public void addPoints(List<Point> points, double minElevation, double maxElevation) {
		boolean first = true;
		int previ = 0;
		int prevj = 0;

		graphics.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		graphics.setComposite(ac);
		graphics.setColor(Color.MAGENTA);

		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];
		int c = 0;
		for (Point point : points) {
			int i = (int) (MAPSPACE.cLonToX(point.getLon(), zoom) - startx);
			int j = (int) (MAPSPACE.cLatToY(point.getLat(), zoom) - starty);
			xPoints[c] = i;
			yPoints[c] = j;
			c++;
			if (!first && previ != i && prevj != j) {
//				graphics.drawLine(previ, prevj, i, j);
				previ = i;
				prevj = j;
			}
			if (first) {
				previ = i;
				prevj = j;
				first = false;
			}
		}
		graphics.drawPolyline(xPoints, yPoints, points.size());
	}

	public void saveImage(String imgPath) throws IOException {
		ImageIO.write(image, "png", new File(imgPath));
	}

}
