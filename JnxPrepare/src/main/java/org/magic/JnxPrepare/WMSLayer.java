package org.magic.JnxPrepare;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.thebuzzmedia.imgscalr.Scalr;

public class WMSLayer {

	private static final int IO_BUFFER_SIZE = 4 * 1024;

	private int zoom;

	private String layer;

	private MagicPower2MapSpace mapSpace = MagicPower2MapSpace.INSTANCE_256;

	public WMSLayer(int zoom, String layer) {
		super();
		this.zoom = zoom;
		this.layer = layer;
	}

	public void computeLayers(double minLon, double maxLon, double minLat,
			double maxLat, Contour contour, String area) throws IOException {
		File tileList = new File(LayerComputer.cacheFolder, "magicCache/"
				+ area + ".list");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tileList));

		int minX = (int) Math
				.floor((1.0 * mapSpace.cLonToX(minLon, zoom)) / 256.0) - 10;
		int minY = (int) Math
				.floor((1.0 * mapSpace.cLatToY(minLat, zoom)) / 256.0) + 10;

		int maxX = (int) Math
				.ceil((1.0 * mapSpace.cLonToX(maxLon, zoom)) / 256.0) + 10;
		int maxY = (int) Math
				.ceil((1.0 * mapSpace.cLatToY(maxLat, zoom)) / 256.0) - 10;

		System.out.println(zoom + " : " + minX + "->" + maxX + " ; " + maxY
				+ "->" + minY);

		for (int x = minX; x <= maxX; x++) {
			for (int y = maxY; y <= minY; y++) {
				double[] coords = MagicTiles.calculateLatLon(mapSpace, zoom, x,
						y);
				if (contour.includes(coords)) {
					computeTile(x, y, zoom, bw);
				}
			}
		}

		bw.close();
		
		System.out.println("Finished " + area);
	}

	private void computeTile(final int x, final int y, final int zoom,
			BufferedWriter bw) throws IOException {
		final File imageFile = new File(LayerComputer.cacheFolder,
				"magicCache/" + zoom + "/" + x + "/" + y + ".jpg");
		final File downloadedFile = new File(LayerComputer.cacheFolder,
				"magicCache/" + zoom + "/" + x + "/" + y + ".png");
		bw.write(".\\" + zoom + "\\" + x + "\\" + y + ".jpg");
		bw.newLine();

		if (!imageFile.exists()) {
			System.out.println(imageFile.getAbsolutePath());
			imageFile.getParentFile().mkdirs();

			while (LayerComputer.workQueue.queueSize() > 7) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ignored) {
				}
			}

			LayerComputer.workQueue.execute(new Runnable() {

				public void run() {
					try {
						if (!downloadedFile.exists()) {

							double[] coords = MagicTiles.calculateLatLon(
									mapSpace, zoom, x, y);

							String url = "http://mapdmz.brgm.fr/cgi-bin/mapserv?map=/carto/infoterre/mapFiles/scan.map&VERSION=1.1.1&SERVICE=WMS&REQUEST=GetMap"
									+ "&LAYERS="
									+ layer
									+ "&SRS=EPSG:4326"
									+ "&FORMAT=image/png&BBOX="
									+ coords[0]
									+ ","
									+ coords[1]
									+ ","
									+ coords[2]
									+ ","
									+ coords[3] + "&WIDTH=512&HEIGHT=512";

							downloadImageFile(downloadedFile, url);
						}

						BufferedImage tileImage = ImageIO.read(downloadedFile);
						BufferedImage resize = Scalr.resize(tileImage, 256);
						writeImage(resize, imageFile);
					} catch (Throwable e) {
						e.printStackTrace();
						imageFile.delete();
					}
					downloadedFile.delete();
				}
			});

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

	public void downloadImageFile(File imageFile, String url)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(imageFile);
		URL remote = new URL(url);
		InputStream is = remote.openStream();

		copyStream(fos, is);
		fos.close();

		System.out.println("downloaded " + url);
	}

	private void copyStream(OutputStream fos, InputStream is)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = is.read(b)) != -1) {
			fos.write(b, 0, read);
		}
		is.close();
	}

}
