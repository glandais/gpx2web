package org.magic.JnxPrepare;

import java.awt.Color;
import java.awt.geom.Point2D.Double;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Set;

import javax.media.jai.PerspectiveTransform;

public class MagicTile {

	private static int COMPUTING_ZOOM = 15;

	static DecimalFormat df;

	private static final int IO_BUFFER_SIZE = 4 * 1024;

	static {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		df = new DecimalFormat("0.##############", symbols);
	}

	private String url;
	private File imageFile;
	private MagicPower2MapSpace mapSpace;

	private Number bl_lon;

	private Number bl_lat;

	private Number br_lon;

	private Number br_lat;

	private Number tl_lon;

	private Number tl_lat;

	private Number tr_lon;

	private Number tr_lat;

	private double x_bl;

	private double y_bl;

	private double x_br;

	private double y_br;

	private double x_tl;

	private double y_tl;

	private double x_tr;

	private double y_tr;

	private PerspectiveTransform perspectiveTransform;

	public MagicTile(MagicPower2MapSpace mapSpace, File file)
			throws ParseException, IOException {
		super();

		this.mapSpace = mapSpace;

		String fileName = file.getName();
		imageFile = new File(file.getParentFile(), fileName.substring(0,
				fileName.length() - 4) + ".png");

		BufferedReader reader = new BufferedReader(new FileReader(file));
		// URL BL.lon BL.lat BR.lon BR.lat TL.lon TL.lat TR.lon TR.lat width
		// height
		reader.readLine();
		url = reader.readLine();
		bl_lon = df.parse(reader.readLine());
		bl_lat = df.parse(reader.readLine());
		br_lon = df.parse(reader.readLine());
		br_lat = df.parse(reader.readLine());
		tl_lon = df.parse(reader.readLine());
		tl_lat = df.parse(reader.readLine());
		tr_lon = df.parse(reader.readLine());
		tr_lat = df.parse(reader.readLine());

		reader.close();

		double x0 = mapSpace.cLonToX(bl_lon.doubleValue(), COMPUTING_ZOOM);
		double y0 = mapSpace.cLatToY(bl_lat.doubleValue(), COMPUTING_ZOOM);

		double x1 = mapSpace.cLonToX(br_lon.doubleValue(), COMPUTING_ZOOM);
		double y1 = mapSpace.cLatToY(br_lat.doubleValue(), COMPUTING_ZOOM);

		double x2 = mapSpace.cLonToX(tr_lon.doubleValue(), COMPUTING_ZOOM);
		double y2 = mapSpace.cLatToY(tr_lat.doubleValue(), COMPUTING_ZOOM);

		double x3 = mapSpace.cLonToX(tl_lon.doubleValue(), COMPUTING_ZOOM);
		double y3 = mapSpace.cLatToY(tl_lat.doubleValue(), COMPUTING_ZOOM);

		perspectiveTransform = PerspectiveTransform.getQuadToSquare(x3, y3, x2,
				y2, x1, y1, x0, y0);
	}

	public File getImageFile() {
		return imageFile;
	}

	public boolean contains(double lon, double lat) {
		Double res = getRelativePos(lon, lat);

		if (res.getX() >= 0 && res.getX() <= 1.0 && res.getY() >= 0
				&& res.getY() <= 1.0) {
			return true;
		}

		return false;
	}

	private Double getRelativePos(double lon, double lat) {
		double x = mapSpace.cLonToX(lon, COMPUTING_ZOOM);
		double y = mapSpace.cLatToY(lat, COMPUTING_ZOOM);

		Double res = new Double();
		perspectiveTransform.transform(new Double(x, y), res);
		return res;
	}

	public Color getColor(LoadedTile loadedTile, double lon, double lat) {
		Double res = getRelativePos(lon, lat);

		double vx = res.getX();
		double vy = res.getY();

		double rx = Math.max(0, Math.min(256, 256 * vx));
		double ry = Math.max(0, Math.min(256, 256 * vy));

		int ix = (int) Math.floor(rx);
		int iy = (int) Math.floor(ry);

		double coefx = rx - ix;
		double coefy = ry - iy;

		Color c = getColorI(loadedTile, ix, iy);
		Color cx = getColorI(loadedTile, ix + 1, iy);
		Color cy = getColorI(loadedTile, ix, iy + 1);
		Color cxy = getColorI(loadedTile, ix + 1, iy + 1);

		int r = composite(c.getRed(), cx.getRed(), cy.getRed(), cxy.getRed(),
				coefx, coefy);
		int g = composite(c.getGreen(), cx.getGreen(), cy.getGreen(),
				cxy.getGreen(), coefx, coefy);
		int b = composite(c.getBlue(), cx.getBlue(), cy.getBlue(),
				cxy.getBlue(), coefx, coefy);

		return new Color(r, g, b);
	}

	private Color getColorI(LoadedTile loadedTile, int ix, int iy) {
		int x = Math.max(0, Math.min(255, ix));
		int y = Math.max(0, Math.min(255, iy));
		return new Color(loadedTile.getTileImage().getRGB(x, y));
	}

	private int composite(int c, int cx, int cy, int cxy, double coefx,
			double coefy) {
		double ct = (1.0 * c) * (1.0 - coefx) + (1.0 * cx) * coefx;
		double cb = (1.0 * cy) * (1.0 - coefx) + (1.0 * cxy) * coefx;

		double col = (1.0 * ct) * (1.0 - coefy) + (1.0 * cb) * coefy;
		col = Math.min(255, Math.max(0, col));
		int icol = (int) col;

		return icol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((imageFile == null) ? 0 : imageFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MagicTile other = (MagicTile) obj;
		if (imageFile == null) {
			if (other.imageFile != null)
				return false;
		} else if (!imageFile.equals(other.imageFile))
			return false;
		return true;
	}

	public void getZooms(Set<Integer> zooms) {
		for (int z = 1; z < 20; z++) {
			double d1 = pixDist(z, bl_lon, bl_lat, br_lon, br_lat);
			double d2 = pixDist(z, bl_lon, bl_lat, tl_lon, tl_lat);
			if (d1 > 256 && d2 > 256) {
				zooms.add(new Integer(z));
				return;
			}
		}

	}

	private double pixDist(int z, Number lon1, Number lat1, Number lon2,
			Number lat2) {
		double x1 = mapSpace.cLonToX(lon1.doubleValue(), z);
		double x2 = mapSpace.cLonToX(lon2.doubleValue(), z);

		double y1 = mapSpace.cLatToY(lat1.doubleValue(), z);
		double y2 = mapSpace.cLatToY(lat2.doubleValue(), z);

		double x = (x1 - x2);
		double y = (y1 - y2);
		return Math.sqrt(x * x + y * y);
	}

	public Number getBl_lon() {
		return bl_lon;
	}

	public Number getBl_lat() {
		return bl_lat;
	}

	public Number getBr_lon() {
		return br_lon;
	}

	public Number getBr_lat() {
		return br_lat;
	}

	public Number getTl_lon() {
		return tl_lon;
	}

	public Number getTl_lat() {
		return tl_lat;
	}

	public Number getTr_lon() {
		return tr_lon;
	}

	public Number getTr_lat() {
		return tr_lat;
	}

	public void downloadImageFile() throws IOException {
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

	public void computePixelBounds(int zoom) {
		x_bl = mapSpace.cLonToX(bl_lon.doubleValue(), zoom);
		y_bl = mapSpace.cLatToY(bl_lat.doubleValue(), zoom);

		x_br = mapSpace.cLonToX(br_lon.doubleValue(), zoom);
		y_br = mapSpace.cLatToY(br_lat.doubleValue(), zoom);

		x_tl = mapSpace.cLonToX(tl_lon.doubleValue(), zoom);
		y_tl = mapSpace.cLatToY(tl_lat.doubleValue(), zoom);

		x_tr = mapSpace.cLonToX(tr_lon.doubleValue(), zoom);
		y_tr = mapSpace.cLatToY(tr_lat.doubleValue(), zoom);
	}

	public boolean intersect(double x1, double y1, double x2, double y2) {
		boolean x_intersect = false;
		boolean y_intersect = false;

		if (intersectInterval(x1, x2, x_bl, x_br)) {
			x_intersect = true;
		}
		if (!x_intersect && intersectInterval(x1, x2, x_tl, x_tr)) {
			x_intersect = true;
		}

		if (intersectInterval(y1, y2, y_bl, y_tl)) {
			y_intersect = true;
		}
		if (!y_intersect && intersectInterval(y1, y2, y_br, y_tr)) {
			y_intersect = true;
		}

		return x_intersect && y_intersect;
	}

	private boolean intersectInterval(double xa1, double xa2, double xb1,
			double xb2) {
		if (xb1 < xb2 && xa1 < xb1 && xa2 < xb1)
			return false;
		if (xb2 < xb1 && xa1 < xb2 && xa2 < xb2)
			return false;

		if (xb1 < xb2 && xa1 > xb2 && xa2 > xb2)
			return false;
		if (xb2 < xb1 && xa1 > xb1 && xa2 > xb1)
			return false;

		return true;
	}

}
