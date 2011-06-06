package org.glandais.garmin.wms2jnx;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

import com.thebuzzmedia.imgscalr.Scalr;

public class WMSLayer {

	private static final int POOL_SIZE = 6;

	private static final int IO_BUFFER_SIZE = 4 * 1024;

	private int zoom;

	private String layer;

	private MagicPower2MapSpace mapSpace = MagicPower2MapSpace.INSTANCE_256;

	private File destFolder;

	static PooledExecutor threadPool = new PooledExecutor(POOL_SIZE);

	final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
			5);

	private String wmsURL;

	public WMSLayer(String wmsURL, String layer, int zoom, File destFolder) {
		super();
		this.zoom = zoom;
		this.layer = layer;
		this.destFolder = destFolder;
		this.wmsURL = wmsURL;
	}

	public void computeLayers(GPXProcessor contour, String area)
			throws IOException {

		double minLon = contour.getMinLon();
		double maxLon = contour.getMaxLon();
		double minLat = contour.getMinLat();
		double maxLat = contour.getMaxLat();

		File tileList = new File(destFolder, area + ".list");
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
				double[] coords = mapSpace.calculateLatLon(zoom, x, y);
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
		final File imageFile = new File(destFolder, zoom + "/" + x + "/" + y
				+ ".jpg");
		final File downloadedFile = new File(destFolder, zoom + "/" + x + "/"
				+ y + ".png");
		bw.write(".\\" + zoom + "\\" + x + "\\" + y + ".jpg");
		bw.newLine();

		if (!imageFile.exists()) {
			System.out.println(imageFile.getAbsolutePath());
			imageFile.getParentFile().mkdirs();

			// while (threadPool.getPoolSize() > 5) {
			// try {
			// Thread.sleep(50);
			// } catch (InterruptedException ignored) {
			// }
			// }

			Runnable runnable = new Runnable() {

				public void run() {
					try {
						if (!downloadedFile.exists()) {

							double[] coords = mapSpace.calculateLatLon(zoom, x,
									y);

							String url = wmsURL + "&LAYERS=" + layer
									+ "&SRS=EPSG:4326"
									+ "&FORMAT=image/png&BBOX=" + coords[0]
									+ "," + coords[1] + "," + coords[2] + ","
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
			};
			try {
				threadPool.execute(runnable);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public void done() {
		threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
	}

}
