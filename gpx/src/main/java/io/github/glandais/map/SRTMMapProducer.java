package io.github.glandais.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.srtm.SRTMHelper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SRTMMapProducer {

	private SRTMHelper srtmHelper;

	public SRTMMapProducer(SRTMHelper srtmHelper) {
		super();
		this.srtmHelper = srtmHelper;
	}

	public MapImage createSRTMMap(GPXPath path, int maxsize, double margin) throws IOException {
		log.info("start createSRTMMap");
		MapImage mapImage = new MapImage(path, margin, maxsize);
		fillWithZ(mapImage);
		addPoints(mapImage, path);
		log.info("end createSRTMMap");
		return mapImage;
	}

	protected void fillWithZ(MapImage mapImage) {
		int width = mapImage.getWidth();
		int height = mapImage.getHeight();
		BufferedImage image = mapImage.getImage();
		double[][] zs = new double[width][];
		double minz = Double.MAX_VALUE;
		double maxz = -Double.MAX_VALUE;
		for (int i = 0; i < width; i++) {
			zs[i] = new double[height];
			for (int j = 0; j < height; j++) {
				double lon = mapImage.getLon(i);
				double lat = mapImage.getLat(j);
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
				if (z <= 0) {

				}
				int rgb = getRgb(getRelativeZ(z, minz, maxz));
				image.setRGB(i, j, rgb);
			}
		}
	}

	protected void addPoints(MapImage mapImage, GPXPath path) {
		List<Point> points = path.getPoints();
		double trackminz = path.getMinElevation();
		double trackmaxz = path.getMaxElevation();

		Graphics2D graphics = mapImage.getGraphics();

		boolean first = true;
		int previ = 0;
		int prevj = 0;

		graphics.setStroke(new BasicStroke(3));
		graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		graphics.setComposite(ac);

		for (Point point : points) {
			int i = mapImage.getX(point.getLon());
			int j = mapImage.getY(point.getLat());
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

}
