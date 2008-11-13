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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

public class SRTMHelper {

	private static SRTMHelper instance = new SRTMHelper();

	public static SRTMHelper getInstance() {
		return instance;
	}

	public static void main(String[] args) throws Exception {
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

		System.out.println(SRTMHelper.getInstance().getElevation(-4.9, 45.1));
		System.out.println(SRTMHelper.getInstance().getElevation(-0.1, 49.9));
		System.out.println(SRTMHelper.getInstance().getElevation(-4.9, 49.9));
		System.out.println(SRTMHelper.getInstance().getElevation(-0.1, 45.1));
		//				System.out.println(SRTMHelper.getInstance().getElevation(-179, 59));
	}

	private String dataFolder = "/opt/srtm/";

	private HttpClient client = null;

	private SRTMHelper() {
		createClient();
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

		while ((line = reader.readLine()) != null) {
			String[] split = line.split(" ");
			for (String elevation : split) {
				sele = Short.parseShort(elevation);
				if (sele == -9999) {
					sele = 0;
				}
				writeShort(sele, bof);
			}
		}

		reader.close();
		asciiResult.delete();
	}

	private void writeShort(short dele, BufferedOutputStream bof)
			throws IOException {
		bof.write((dele >>> 8) & 0xFF);
		bof.write((dele >>> 0) & 0xFF);
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
		saveFile(url, zipFile);
		unzip(zipFile);
	}

	public double getElevation(double lon, double lat) throws SRTMException {
		double val = 0;
		try {
			File tile = getTile(lat, lon);
			long ilon = getILon(lon);
			long ilat = getILat(lat);

			double dlon = lon - ((5 * (ilon - 1)) - 180);
			double dlat = (65 - (5 * ilat)) - lat;

			double dcol = Math.max(0, Math.min(5998.999, (6000 * dlon) / 5));
			double drow = Math.max(0, Math.min(5998.999, (6000 * dlat) / 5));

			int colmin = (int) Math.round(Math.floor(dcol));
			double coefcolmin = dcol - colmin;

			int rowmin = (int) Math.round(Math.floor(drow));
			double coefrowmin = drow - rowmin;

			short[] values = new short[4];
			values[0] = getValues(tile, colmin, rowmin);
			values[1] = getValues(tile, colmin + 1, rowmin);
			values[2] = getValues(tile, colmin, rowmin + 1);
			values[3] = getValues(tile, colmin + 1, rowmin + 1);

			double val1 = values[0] * coefcolmin + values[1] * (1 - coefcolmin);
			double val2 = values[2] * coefcolmin + values[3] * (1 - coefcolmin);

			val = val1 * coefrowmin + val2 * (1 - coefrowmin);
		} catch (Exception e) {
			throw new SRTMException(e);
		}
		return val;
	}

	private long getILat(double lat) {
		double dlat = (60 - lat) / 5.0;
		long ilat = Math.round(Math.ceil(dlat));
		return ilat;
	}

	private long getILon(double lon) {
		double dlon = (lon + 180) / 5.0;
		long ilon = Math.round(Math.ceil(dlon));
		return ilon;
	}

	private File getTile(double lat, double lon) throws Exception {
		long ilon = getILon(lon);
		long ilat = getILat(lat);

		String slon = Long.toString(ilon);
		String slat = Long.toString(ilat);
		if (slon.length() == 1) {
			slon = "0" + slon;
		}
		if (slat.length() == 1) {
			slat = "0" + slat;
		}
		String fileName = "srtm_" + slon + "_" + slat;
		File result = new File(dataFolder + fileName + ".bin");
		if (!result.exists()) {
			File asciiResult = new File(dataFolder + fileName + ".ASC");
			if (!asciiResult.exists()) {
				downloadASCIITile(fileName);
			}
			createBin(asciiResult, result);
		}

		return result;
	}

	private short getValues(File tile, int colmin, int rowmin) throws Exception {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				tile));
		long starti = (rowmin * 6000) + colmin;
		bis.skip(2 * starti);
		short readShort = readShort(bis);
		bis.close();
		return readShort;
	}

	private short readShort(BufferedInputStream bis) throws IOException {
		int ch1 = bis.read();
		int ch2 = bis.read();
		return (short) ((ch1 << 8) + (ch2 << 0));
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
				InputStream in = new BufferedInputStream(get
						.getResponseBodyAsStream());
				OutputStream out = new BufferedOutputStream(
						new FileOutputStream(file));
				byte[] buffer = new byte[1024];
				int count = -1;
				while ((count = in.read(buffer)) != -1) {
					out.write(buffer, 0, count);
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
