package org.magic.JnxPrepare;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class MagicTiles {

	private List<MagicTile> tiles = new ArrayList<MagicTile>();

	private Map<MagicTile, LoadedTile> loadedTiles = Collections
			.synchronizedMap(new HashMap<MagicTile, LoadedTile>());

	private MagicPower2MapSpace mapSpace;

	private HashSet<Integer> zooms;

	private MagicTile lastTile;

	public MagicTiles(MagicPower2MapSpace mapSpace, File searchFolder) {
		super();
		this.mapSpace = mapSpace;
		addTiles(searchFolder);
	}

	public void computeLayers(double minLon, double maxLon, double minLat,
			double maxLat, GPXProcessor contour) {
		zooms = new HashSet<Integer>();

		for (MagicTile tile : tiles) {
			tile.getZooms(zooms);
		}

		int zoom = 1;
		for (Integer curZoom : zooms) {
			zoom = Math.max(zoom, curZoom);
		}

		int minX = (int) Math
				.floor((1.0 * mapSpace.cLonToX(minLon, zoom)) / 256.0);
		int minY = (int) Math
				.floor((1.0 * mapSpace.cLatToY(minLat, zoom)) / 256.0);

		int maxX = (int) Math
				.ceil((1.0 * mapSpace.cLonToX(maxLon, zoom)) / 256.0);
		int maxY = (int) Math
				.ceil((1.0 * mapSpace.cLatToY(maxLat, zoom)) / 256.0);

		System.out.println(zoom + " : " + minX + "->" + maxX + " ; " + maxY
				+ "->" + minY);

		for (MagicTile magicTile : tiles) {
			magicTile.computePixelBounds(zoom);
		}

		for (int x = minX; x <= maxX; x++) {
			for (int y = maxY; y <= minY; y++) {
				double[] coords = calculateLatLon(mapSpace, zoom, x, y);
				if (contour.includes(coords)) {
					computeTile(x, y, zoom);
				}
			}
		}
	}

	public static double[] calculateLatLon(MagicPower2MapSpace mapSpace,
			int zoom, int tilex, int tiley) {
		int tileSize = mapSpace.getTileSize();
		double[] result = new double[4];
		tilex *= tileSize;
		tiley *= tileSize;
		result[0] = mapSpace.cXToLon(tilex, zoom); // lon_min
		result[1] = mapSpace.cYToLat(tiley + tileSize, zoom); // lat_max
		result[2] = mapSpace.cXToLon(tilex + tileSize, zoom); // lon_min
		result[3] = mapSpace.cYToLat(tiley, zoom); // lat_max
		return result;
	}

	private void computeTile(int x, int y, int zoom) {
		File imageFile = new File(LayerComputer.cacheFolder, "magicCache/"
				+ zoom + "/" + x + "/" + y + ".jpg");
		if (!imageFile.exists()) {
			System.out.println(imageFile.getAbsolutePath());
			imageFile.getParentFile().mkdirs();

			double[] coords = calculateLatLon(mapSpace, zoom, x, y);

			double x1 = mapSpace.cLonToX(coords[0], zoom);
			double y1 = mapSpace.cLatToY(coords[1], zoom);

			double x2 = mapSpace.cLonToX(coords[2], zoom);
			double y2 = mapSpace.cLatToY(coords[3], zoom);

			int tileSize = 256;

			BufferedImage image = new BufferedImage(tileSize, tileSize,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = image.createGraphics();

			List<MagicTile> matchingTiles = new ArrayList<MagicTile>();
			try {
				for (MagicTile magicTile : tiles) {
					if (magicTile.intersect(x1, y1, x2, y2)) {
						matchingTiles.add(magicTile);
					}
				}

				g2.setColor(Color.WHITE);
				g2.fillRect(0, 0, tileSize - 1, tileSize - 1);

				for (int xi = 0; xi < tileSize; xi++) {
					double lon = coords[0] + (xi / (1.0 * tileSize))
							* (coords[2] - coords[0]);
					for (int yi = 0; yi < tileSize; yi++) {
						double lat = coords[3] + (yi / (1.0 * tileSize))
								* (coords[1] - coords[3]);
						image.setRGB(xi, yi,
								getColorTiles(matchingTiles, lon, lat, zoom)
										.getRGB());
					}
				}

				writeImage(image, imageFile);
			} catch (IOException e) {
				e.printStackTrace();
				imageFile.delete();
			} finally {
				g2.dispose();
			}
		}
	}

	private void writeImage(BufferedImage image, File imageFile)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(imageFile);

		ImageWriter writer = null;
		Iterator<?> iter = ImageIO.getImageWritersByFormatName("JPEG");
		if (!iter.hasNext())
			throw new IOException("No Writers Available");
		writer = (ImageWriter) iter.next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
		writer.setOutput(ios);
		JPEGImageWriteParam iwp = new JPEGImageWriteParam(null);
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.95f);
		writer.write(null, new IIOImage(image, null, null), iwp);
		ios.flush();
		writer.dispose();
		ios.close();
	}

	public HashSet<Integer> getZooms() {
		return zooms;
	}

	public void setZooms(HashSet<Integer> zooms) {
		this.zooms = zooms;
	}

	private void addTiles(File file) {
		if (file.isFile() && file.getName().endsWith(".wld")) {
			try {
				MagicTile magicTile = new MagicTile(mapSpace, file);
				tiles.add(magicTile);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		} else {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File file2 : files) {
					if (!file2.getName().startsWith(".")) {
						addTiles(file2);
					}
				}
			}
		}
	}

	public Color getColorTiles(List<MagicTile> matchingTiles, double lon,
			double lat, int zoom) throws IOException {
		Color result = Color.WHITE;
		if (lastTile != null && lastTile.contains(lon, lat)) {
			return getColorTile(lastTile, lon, lat, zoom);
		} else {
			for (MagicTile magicTile : matchingTiles) {
				if (magicTile.contains(lon, lat)) {
					lastTile = magicTile;
					return getColorTile(magicTile, lon, lat, zoom);
				}
			}
		}
		return result;
	}

	private Color getColorTile(MagicTile magicTile, double lon, double lat,
			int zoom) throws IOException {
		Color result = Color.WHITE;
		LoadedTile loadedTile = getLoadTile(magicTile);
		result = magicTile.getColor(loadedTile, lon, lat);
		return result;
	}

	private LoadedTile getLoadTile(MagicTile magicTile) throws IOException {
		LoadedTile loadedTile = loadedTiles.get(magicTile);
		if (loadedTile == null) {
			removeOldest();
			loadedTile = new LoadedTile(magicTile);
			loadedTiles.put(magicTile, loadedTile);
		}
		loadedTile.setLastAccess(new Date());
		return loadedTile;
	}

	private void removeOldest() {
		while (loadedTiles.size() > 100) {
			List<LoadedTile> values = new ArrayList<LoadedTile>(
					loadedTiles.values());
			Collections.sort(values, new Comparator<LoadedTile>() {
				public int compare(LoadedTile o1, LoadedTile o2) {
					return o1.getLastAccess().compareTo(o2.getLastAccess());
				}
			});

			LoadedTile toRemove = values.get(0);
			loadedTiles.remove(toRemove.getTile());
		}
	}

}
