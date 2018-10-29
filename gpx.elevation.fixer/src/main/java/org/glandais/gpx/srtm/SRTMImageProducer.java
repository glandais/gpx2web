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

public class SRTMImageProducer {

	private BufferedImage image;

	private double minlon;
	private double minlat;
	private double maxlon;
	private double maxlat;
	private int width;
	private int height;
	private double minz = Double.MAX_VALUE;
	private double maxz = -Double.MAX_VALUE;

	private Graphics2D graphics;

	public static void main(String[] args) throws Exception {
		// SRTMImageProducer imageProducer = new SRTMImageProducer(-2, -1, 46.5,
		// 47.5, 200, 0);

		// System.out.println(SRTMHelper.getInstance().getElevation(4,
		// 44.9997));
		// System.out.println(SRTMHelper.getInstance().getElevation(4.0005,
		// 44.9997));
		// System.out.println(SRTMHelper.getInstance().getElevation(4.0005,
		// 45.0003));
		// System.out.println(SRTMHelper.getInstance().getElevation(4,
		// 45.0003));

		SRTMImageProducer imageProducer = new SRTMImageProducer(0.0001, 4.9999, 45.0001, 45.9999, 200, 0);
		imageProducer.fillWithZ();
		imageProducer.saveImage("/tmp/map.png");

	}

	public void saveImage(String fileName) throws IOException {
		ImageIO.write(image, "png", new File(fileName));
	}

	public SRTMImageProducer(double minlon, double maxlon, double minlat, double maxlat, int maxsize, double margin) {
		super();

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

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();
	}

	public void fillWithZ() throws SRTMException {
		double[][] zs = new double[width][];
		for (int i = 0; i < width; i++) {
			zs[i] = new double[height];
			for (int j = 0; j < height; j++) {
				double lon = getLon(i);
				double lat = getLat(j);
				double z = SRTMHelper.getInstance().getElevation(lon, lat);
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
				if (z <= 0) {

				}
				int rgb = getRgb(getRelativeZ(z, minz, maxz));
				image.setRGB(i, j, rgb);
			}
		}
	}

	private double getRelativeZ(double z, double minz, double maxz) {
		return (z - minz) / (maxz - minz);
	}

	private int getRgb(double d) {
		/*
		 * int r = (int) Math.round(255 * d); int g = r; int b = r;
		 */
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

	private double getLat(int j) {
		return minlat + (1.0 * (height - 1 - j) * (maxlat - minlat)) / height;
	}

	private double getLon(int i) {
		return minlon + (1.0 * i * (maxlon - minlon)) / width;
	}

	public void addPoints(List<? extends Point> points, double trackminz, double trackmaxz) {
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

	private int getColor(double z) {
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

	private int getJ(double lat) {
		int j = (int) Math.round(1.0 * height * (maxlat - lat) / (maxlat - minlat));
		return j;
	}

	private int getI(double lon) {
		int i = (int) Math.round(1.0 * width * (lon - minlon) / (maxlon - minlon));
		return i;
	}

}
