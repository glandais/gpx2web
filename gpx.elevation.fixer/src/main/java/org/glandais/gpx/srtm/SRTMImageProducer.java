package org.glandais.gpx.srtm;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.glandais.gpx.elevation.fixer.GPXPath;

public class SRTMImageProducer {

	protected BufferedImage image;

	private GPXPath gpxPath;

	protected double minlon;
	protected double minlat;
	protected double maxlon;
	protected double maxlat;
	protected int width;
	protected int height;
	protected double minz = Double.MAX_VALUE;
	protected double maxz = -Double.MAX_VALUE;

	protected Graphics2D graphics;

	protected SRTMHelper srtmHelper;

//	public static void main(String[] args) throws Exception {
//		SRTMHelper srtmHelper = new SRTMHelper(new File("cache" + File.separator + "srtm"));
//		SRTMImageProducer imageProducer = new SRTMImageProducer(srtmHelper, 0.0001, 4.9999, 45.0001, 45.9999, 200, 0);
//		imageProducer.fillWithZ();
//		imageProducer.saveImage("/tmp/map.png");
//	}

	public SRTMImageProducer(SRTMHelper srtmHelper, GPXPath gpxPath, int maxsize, double margin) {
		super();
		this.gpxPath = gpxPath;
		double minlon = gpxPath.getMinlon();
		double maxlon = gpxPath.getMaxlon();
		double minlat = gpxPath.getMinlat();
		double maxlat = gpxPath.getMaxlat();

		this.srtmHelper = srtmHelper;

		double lonmiddle = (maxlon + minlon) / 2;
		double lonwidht = (maxlon - minlon) * (1.0 + margin);
		this.minlon = lonmiddle - lonwidht / 2.0;
		this.maxlon = lonmiddle + lonwidht / 2.0;

		double latmiddle = (maxlat + minlat) / 2;
		double latwidht = (maxlat - minlat) * (1.0 + margin);
		this.minlat = latmiddle - latwidht / 2.0;
		this.maxlat = latmiddle + latwidht / 2.0;

		if (lonwidht > latwidht) {
			this.width = maxsize;
			this.height = (int) Math.round((1.0 * maxsize * (maxlat - minlat)) / (maxlon - minlon));
		} else {
			this.height = maxsize;
			this.width = (int) Math.round((1.0 * maxsize * (maxlon - minlon)) / (maxlat - minlat));
		}

		if (width > 0 && height > 0) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			graphics = image.createGraphics();
		}
	}

	public void export(String imgPath) throws IOException {
		if (width > 0 && height > 0) {
			fillWithZ();
			addPoints(gpxPath.getPoints(), gpxPath.getMinElevation(), gpxPath.getMaxElevation());
			saveImage(imgPath);
		}
	}

	protected void saveImage(String fileName) throws IOException {
		ImageIO.write(image, "png", new File(fileName));
	}

	protected void fillWithZ() {
		double[][] zs = new double[width][];
		for (int i = 0; i < width; i++) {
			zs[i] = new double[height];
			for (int j = 0; j < height; j++) {
				double lon = getLon(i);
				double lat = getLat(j);
				double z = srtmHelper.getElevation(lon, lat);
				if (z < minz) {
					minz = z;
				}
				if (z > maxz) {
					maxz = z;
				}
				zs[i][j] = z;
			}
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double z = zs[i][j];
				int rgb = getRgb(getRelativeZ(z, minz, maxz));
				image.setRGB(i, j, rgb);
			}
		}
	}

	protected double getRelativeZ(double z, double minz, double maxz) {
		return (z - minz) / (maxz - minz);
	}

	protected int getRgb(double d) {
		int r = 0;
		int g = 0;
		int b = 0;
		if (d < 0.5) {
			r = (int) Math.round(511 * d);
			g = 255;
			b = 255 - r;
		} else {
			r = 255;
			b = (int) Math.round(511 * (d - 0.5));
			g = 255 - b;
		}

		return (r << 16) + (g << 8) + b;
	}

	protected double getLat(int j) {
		return minlat + (1.0 * (height - 1 - j) * (maxlat - minlat)) / height;
	}

	protected double getLon(int i) {
		return minlon + (1.0 * i * (maxlon - minlon)) / width;
	}

	protected void addPoints(List<? extends Point> points, double trackminz, double trackmaxz) {
		boolean first = true;
		int previ = 0;
		int prevj = 0;

		graphics.setStroke(new BasicStroke(3));
		graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		graphics.setComposite(ac);

		for (Point point : points) {
			int i = getI(point.getLon());
			int j = getJ(point.getLat());
			if (!first) {
				int c = getColor(getRelativeZ(point.getZ(), trackminz, trackmaxz));
				graphics.setColor(new Color(c));
				graphics.drawLine(previ, prevj, i, j);
			}
			previ = i;
			prevj = j;
			first = false;
		}
	}

	protected int getColor(double z) {
		int r = 0;
		int g = 0;
		int b = 0;
		if (z < 0.5) {
			r = 0;
			g = (int) Math.round(511 * z);
			b = 255 - g;
		} else {
			r = (int) Math.round(511 * (z - 0.5));
			g = 255 - r;
			b = 0;
		}
		return (r << 16) + (g << 8) + b;
	}

	protected int getJ(double lat) {
		return (int) Math.round(1.0 * height * (maxlat - lat) / (maxlat - minlat));
	}

	protected int getI(double lon) {
		return (int) Math.round(1.0 * width * (lon - minlon) / (maxlon - minlon));
	}

}
