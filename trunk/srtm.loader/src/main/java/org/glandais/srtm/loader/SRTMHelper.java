package org.glandais.srtm.loader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

public class SRTMHelper {

	private String dataFolder = "D://srtm//";

	private static SRTMHelper instance = new SRTMHelper();

	private File[][] tiles;

	private static final boolean debugging = false;

	public static SRTMHelper getInstance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
		/*
		System.out.println(SRTMHelper.getInstance().getElevation(6.864167,
				45.8325));
		System.out.println(SRTMHelper.getInstance().getElevation(2.35, 48.9));
		System.out.println(SRTMHelper.getInstance().getElevation(-1.55278,
				47.21806));
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				double elevation = SRTMHelper.getInstance().getElevation(
						5.05 + 0.49 * i, 45.05 + 0.49 * j);
				elevation = Math.round(elevation);
				System.out.print(elevation);
				System.out.print(" ");
			}
			System.out.println();
		}
		*/
		System.out.println(SRTMHelper.getInstance().getElevation(-5, 45));
		System.out.println(SRTMHelper.getInstance().getElevation(
				-4.999999999999, 45.000000000001));
		System.out.println(SRTMHelper.getInstance().getElevation(
				-0.000000000001, 49.999999999999));
		System.out.println(SRTMHelper.getInstance().getElevation(0, 50));
		System.out.println(SRTMHelper.getInstance().getElevation(
				-4.999999999999, 49.999999999999));
		System.out.println(SRTMHelper.getInstance().getElevation(-5, 50));
		System.out.println(SRTMHelper.getInstance().getElevation(
				-0.000000000001, 45.000000000001));
		System.out.println(SRTMHelper.getInstance().getElevation(0, 45));
		// http://maps.google.fr/?ie=UTF8&ll=,&spn=0.008277,0.022745&z=16
		// http://maps.google.fr/?ie=UTF8&ll=47.227357,-1.547876&spn=0.008277,0.022745&z=16
		System.out.println(SRTMHelper.getInstance().getElevation(-1.547876,
				47.227357));

		// double d = 5.0d / 6000.0d;
		//
		// double lon = -1857 * d;
		// double lat = 56672 * d;
		//
		// System.out.println(SRTMHelper.getInstance().getElevation(lon, lat));
		// System.out.println(SRTMHelper.getInstance().getElevation(lon + d,
		// lat));
		// System.out.println(SRTMHelper.getInstance().getElevation(lon + d,
		// lat - d));
		// System.out.println(SRTMHelper.getInstance().getElevation(lon, lat -
		// d));
		//
		// System.out.println(SRTMHelper.getInstance().getElevation(lon + d /
		// 2.0,
		// lat - d / 2.0));

		// System.out.println(SRTMHelper.getInstance().getElevation(-179, 59));
	}

	private HttpClient client = null;

	private SRTMHelper() {
		createClient();
		tiles = new File[72][];
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = new File[36];
			for (int j = 0; j < tiles[i].length; j++) {
				String fileName = getFileName(i, j);
				tiles[i][j] = new File(dataFolder + fileName + ".bin");
			}
		}
	}

	private void createClient() {
		System.getProperties().setProperty("httpclient.useragent",
				"Mozilla/4.0");
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
	}

	private void downloadASCIITile(String fileName) throws Exception {
		String url = "http://hypersphere.telascience.org/elevation/cgiar_srtm_v4/ascii/zip/"
				+ fileName + ".ZIP";
		File zipFile = new File(dataFolder + fileName + ".ZIP");
		if (!zipFile.exists()) {
			saveFile(url, zipFile);
		}
		unzip(zipFile);
	}

	public double getElevation(double lon, double lat) throws SRTMException {
		double val = 0;
		try {
			double dcol = lonToCol(lon);
			double drow = latToRow(lat);

			int colmin = (int) Math.round(Math.floor(dcol));
			double coefcolmin = dcol - colmin;

			int rowmin = (int) Math.round(Math.floor(drow));
			double coefrowmin = drow - rowmin;

			short[] values = new short[4];
			values[0] = getValue(colmin, rowmin);
			values[1] = getValue(colmin + 1, rowmin);
			values[2] = getValue(colmin, rowmin + 1);
			values[3] = getValue(colmin + 1, rowmin + 1);

			double val1 = values[0] * (1 - coefcolmin) + values[1] * coefcolmin;
			double val2 = values[2] * (1 - coefcolmin) + values[3] * coefcolmin;

			val = val1 * (1 - coefrowmin) + val2 * coefrowmin;
		} catch (Exception e) {
			throw new SRTMException(e);
		}
		return val;
	}

	private double latToRow(double lat) {
		return (6000 * (60 - lat)) / 5;
	}

	private double lonToCol(double lon) {
		return (6000 * (180 + lon)) / 5;
	}

	private double rowToLat(double row) {
		return 60 - ((row * 5.0) / 6000.0);
	}

	private double colToLon(double col) {
		return ((col * 5.0) / 6000.0) - 180;
	}

	public List<Point> getPointsBetween(final Point p1, Point p2)
			throws SRTMException {
		List<Point> result = new ArrayList<Point>();
		result.add(p1);
		result.add(p2);

		p1.setZ(getElevation(p1.getLon(), p1.getLat()));
		p2.setZ(getElevation(p2.getLon(), p2.getLat()));

		double dcol1 = lonToCol(p1.getLon());
		double drow1 = latToRow(p1.getLat());

		double dcol2 = lonToCol(p2.getLon());
		double drow2 = latToRow(p2.getLat());

		int mincol = Math.min((int) Math.round(Math.floor(dcol1)),
				(int) Math.round(Math.floor(dcol2)));
		int maxcol = Math.max((int) Math.round(Math.floor(dcol1)),
				(int) Math.round(Math.floor(dcol2)));
		int minrow = Math.min((int) Math.round(Math.floor(drow1)),
				(int) Math.round(Math.floor(drow2)));
		int maxrow = Math.max((int) Math.round(Math.floor(drow1)),
				(int) Math.round(Math.floor(drow2)));

		if (Math.abs(dcol1 - dcol2) > 0.001) {
			for (int col = mincol + 1; col <= maxcol; col++) {
				double c = (col - dcol1) / (dcol2 - dcol1);
				double drow = drow1 + c * (drow2 - drow1);
				Point p = new Point(colToLon(col), rowToLat(drow));
				p.setZ(getElevation(p.getLon(), p.getLat()));
				result.add(p);
			}
		}
		if (Math.abs(drow1 - drow2) > 0.001) {
			for (int row = minrow + 1; row <= maxrow; row++) {
				double c = (row - drow1) / (drow2 - drow1);
				double dcol = dcol1 + c * (dcol2 - dcol1);
				Point p = new Point(colToLon(dcol), rowToLat(row));
				p.setZ(getElevation(p.getLon(), p.getLat()));
				result.add(p);
			}
		}

		Collections.sort(result, new Comparator<Point>() {
			public int compare(Point cp1, Point cp2) {
				double d1 = p1.distanceTo(cp1);
				double d2 = p1.distanceTo(cp2);
				return Double.compare(d1, d2);
			}
		});

		return result;
	}

	private short getValue(int colmin, int rowmin) throws Exception {
		int ilon = (int) Math.round(Math.ceil(colmin / 6000.0));
		int ilat = (int) Math.round(Math.ceil(rowmin / 6000.0));
		File tile = getTile(ilon, ilat);
		int col = colmin - 6000 * (ilon - 1);
		int row = rowmin - 6000 * (ilat - 1);

		boolean changed = false;
		if (col > 5999) {
			ilon = ilon + 1;
			changed = true;
		}
		if (row > 5999) {
			ilat = ilat + 1;
			changed = true;
		}
		if (changed) {
			tile = getTile(ilon, ilat);
			col = colmin - 6000 * (ilon - 1);
			row = rowmin - 6000 * (ilat - 1);
		}

		short result = getValue(tile, col, row);
		return result;
	}

	private File getTile(int ilon, int ilat) throws Exception {
		File result = tiles[ilon][ilat];
		if (!result.exists()) {
			downloadTile(ilon, ilat, result);
		}
		return result;
	}

	private short getValue(File tile, int col, int row) throws Exception {
		FileInputStream fis = new FileInputStream(tile);
		long starti = (row * 6000) + col;
		fis.skip(2 * starti);
		short readShort = readShort(fis);
		fis.close();
		return readShort;
	}

	private short readShort(FileInputStream bis) throws IOException {
		int ch1 = bis.read();
		int ch2 = bis.read();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	private void downloadTile(int ilon, int ilat, File result) throws Exception {
		String fileName = getFileName(ilon, ilat);
		File asciiResult = new File(dataFolder + fileName + ".ASC");
		if (!asciiResult.exists()) {
			downloadASCIITile(fileName);
		}
		createBin(asciiResult, result);
	}

	private void createBin(File asciiResult, File result) throws Exception {
		FileReader fileReader;
		fileReader = new FileReader(asciiResult);
		BufferedReader reader = new BufferedReader(fileReader);
		for (int i = 0; i < 6; i++) {
			reader.readLine();
		}
		String line = null;
		short sele = 0;

		BufferedOutputStream bof = new BufferedOutputStream(
				new FileOutputStream(result), 1024 * 1024);

		int l = 0;
		while ((line = reader.readLine()) != null) {
			String[] split = line.split(" ");
			for (String elevation : split) {
				sele = Short.parseShort(elevation);
				if (sele == -9999) {
					sele = 0;
				}
				writeShort(sele, bof);
			}
			// System.out.println(l + " - " + split.length);
			l++;
		}
		bof.close();
		reader.close();
		if (!debugging) {
			asciiResult.delete();
		}
	}

	private void writeShort(short dele, BufferedOutputStream bof)
			throws IOException {
		bof.write((dele >>> 8) & 0xFF);
		bof.write((dele >>> 0) & 0xFF);
	}

	private String getFileName(int ilon, int ilat) {
		String slon = Long.toString(ilon);
		String slat = Long.toString(ilat);
		if (slon.length() == 1) {
			slon = "0" + slon;
		}
		if (slat.length() == 1) {
			slat = "0" + slat;
		}
		String fileName = "srtm_" + slon + "_" + slat;
		return fileName;
	}

	private void saveFile(String url, File file) throws Exception {
		GetMethod get = new GetMethod(url);
		get.setRequestHeader("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");

		get.setFollowRedirects(true);
		Exception tothrow = null;
		try {
			client.executeMethod(get);
			if (get.getStatusCode() != 404) {
				long grandtotal = -1;
				long total = 0;
				long previoustotal = 0;
				long previousTime = System.currentTimeMillis();
				long previouspercent = 0;
				if (get.getResponseContentLength() > 0) {
					grandtotal = get.getResponseContentLength();
				}
				InputStream in = new BufferedInputStream(
						get.getResponseBodyAsStream());
				OutputStream out = new BufferedOutputStream(
						new FileOutputStream(file));
				byte[] buffer = new byte[1024];
				int count = -1;
				while ((count = in.read(buffer)) != -1) {
					out.write(buffer, 0, count);
					if (grandtotal > -1) {
						total += count;
						long percent = (100 * total) / grandtotal;
						if (previouspercent != percent) {
							long time = System.currentTimeMillis();

							long difftotal = total - previoustotal;
							long difftime = time - previousTime;
							long speed = -1;
							if (difftime > 0) {
								speed = difftotal / difftime;
							}
							System.out.println(file.getAbsolutePath() + " : "
									+ percent + "% (" + speed + "kB/s)");

							previouspercent = percent;
							previoustotal = total;
							previousTime = time;
						}
					}
				}
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			tothrow = e;
		} finally {
			createClient();
			get.releaseConnection();
		}
		if (tothrow != null) {
			throw tothrow;
		}
	}

	private void unzip(File zipFile) throws Exception {
		BufferedOutputStream dest = null;
		FileInputStream fis = new FileInputStream(zipFile);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[2048];
			FileOutputStream fos = new FileOutputStream(dataFolder
					+ entry.getName());
			dest = new BufferedOutputStream(fos, 2048);
			while ((count = zis.read(data, 0, 2048)) != -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
		}
		zis.close();
		zipFile.delete();
	}

}
