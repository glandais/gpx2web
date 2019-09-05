package io.github.glandais.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.util.Point2D;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TileMapProducer {

	protected CloseableHttpClient httpClient;

	@Value("${gpx.data.cache:cache}")
	private File cacheFolder = new File("cache");

	protected static final String SEPARATOR = File.separator;

	protected static final String ABC = "abc";

	public TileMapProducer() {
		super();
		httpClient = HttpClientBuilder.create().setUserAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36")
				.build();
	}

	public TileMapImage createTileMap(GPXPath path, String urlPattern, int zoom, double margin) throws IOException {
		log.info("start createTileMap");
		TileMapImage tileMapImage = new TileMapImage(path, margin, cacheFolder, urlPattern, zoom);

		log.info("Creating a map of {}x{} pixels", tileMapImage.getWidth(), tileMapImage.getHeight());

		fillWithImages(tileMapImage);
		addPoints(tileMapImage, path);

		log.info("end createTileMap");
		return tileMapImage;
	}

	protected void fillWithImages(TileMapImage tileMapImage) throws IOException {
		double startx = tileMapImage.getStartx();
		double starty = tileMapImage.getStarty();

		int timin = (int) Math.floor(tileMapImage.getTileI(tileMapImage.getMinlon()));
		int timax = (int) Math.ceil(tileMapImage.getTileI(tileMapImage.getMaxlon()));

		int tjmin = (int) Math.floor(tileMapImage.getTileJ(tileMapImage.getMaxlat()));
		int tjmax = (int) Math.ceil(tileMapImage.getTileJ(tileMapImage.getMinlat()));

		for (int i = timin; i < timax; i++) {
			for (int j = tjmin; j < tjmax; j++) {
				BufferedImage img = getImage(tileMapImage, i, j);
				if (img != null) {
					double x = i * 256 - startx;
					double y = j * 256 - starty;
					tileMapImage.getGraphics().drawImage(img, (int) x, (int) y, null);
				}
			}
		}
	}

	protected BufferedImage getImage(TileMapImage tileMapImage, int i, int j) throws IOException {
		File cache = tileMapImage.getCache();
		int zoom = tileMapImage.getZoom();
		String urlPattern = tileMapImage.getUrlPattern();
		File tile = new File(cache, zoom + SEPARATOR + i + SEPARATOR + j);
		if (!tile.exists()) {
			String url = urlPattern.replace("{z}", "" + zoom).replace("{x}", "" + i).replace("{y}", "" + j)
					.replace("{s}", "" + ABC.charAt(ThreadLocalRandom.current().nextInt(3)));
			tile.getParentFile().mkdirs();
			log.info("Downloading {}", url);
			try {
				try (CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
						OutputStream outputStream = new FileOutputStream(tile);) {
					InputStream inputStream = response.getEntity().getContent();
					IOUtils.copy(inputStream, outputStream);
				}
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

	protected void addPoints(TileMapImage tileMapImage, GPXPath path) {
		Graphics2D graphics = tileMapImage.getGraphics();

		graphics.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		graphics.setComposite(ac);
		graphics.setColor(Color.MAGENTA);

		drawPath(tileMapImage, path);
		drawArrows(tileMapImage, path);
	}

	private void drawPath(TileMapImage tileMapImage, GPXPath path) {
		List<Point> points = path.getPoints();

		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];
		int c = 0;
		for (Point point : points) {
			int i = tileMapImage.getX(point.getLon());
			int j = tileMapImage.getY(point.getLat());
			xPoints[c] = i;
			yPoints[c] = j;
			c++;
		}
		tileMapImage.getGraphics().drawPolyline(xPoints, yPoints, points.size());
	}

	private void drawArrows(TileMapImage tileMapImage, GPXPath gpxPath) {
		Graphics2D graphics = tileMapImage.getGraphics();
		double[] dists = gpxPath.getDists();
		double length = dists[dists.length - 1];
		int count = 5;
		double arrowSizeX = 32;
		double arrowSizeY = 32;
		List<Point> points = gpxPath.getPoints();
		int c = 0;
		List<Point2D> checkpoints = new ArrayList<>();

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
				int x = tileMapImage.getX(p.getLon());
				int y = tileMapImage.getY(p.getLat());
				checkpoints.add(new Point2D(x, y));
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
				Point2D pm1 = checkpoints.get(i * 3);
				Point2D p = checkpoints.get(i * 3 + 1);
				Point2D pp1 = checkpoints.get(i * 3 + 2);

				double x0 = p.getX();
				double y0 = p.getY();

				double xdx = pp1.getX() - pm1.getX();
				double xdy = pp1.getY() - pm1.getY();
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
}
